package ru.gosuslugi.pgu.fs.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Tests for {@link PersonOkatoServiceImpl} methods
 * проверка работы Singleton-а
 * @author ebalovnev
 */
public class PersonOkatoServiceImplSingletonTest {

    private PersonOkatoServiceImpl personOkatoService;

    @Before
    public void init () {
        personOkatoService = Mockito.mock(PersonOkatoServiceImpl.class);
    }

    @Test
    public void testAddress() throws ExecutionException, InterruptedException {
        int threadCount = 10;
        String newOkato = "new_okato";
        String badOkato = "bad_okato";

        Mockito.when(personOkatoService.getOkato())
            .thenCallRealMethod();
        Mockito.when(personOkatoService.calculate())
            .thenAnswer(
                new Answer<String>() {
                    private int index = 0;
                    @Override
                    public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Thread.sleep(50L);
                        String result = index == 0 ? newOkato : badOkato;
                        index++;
                        return result;
                    }
                }
            );

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        Collection<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            // Параллельный запрос ОКАТО из нескольких потоков
            futures.add(executorService.submit(() -> personOkatoService.getOkato()));
        }
        // Должен быть только нулевой ответ
        for (Future<String> future : futures) {
            Assert.assertEquals(newOkato, future.get());
        }

        // аккуратное закрытие executorService
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // С точки зрения теста данное событие не страшно: это подстраховка на случай проблем с закрытием executorService
        }

        // Проверяем замоканные вызванные функции
        Mockito.verify(personOkatoService, Mockito.times(threadCount))
            .getOkato();
        Mockito.verify(personOkatoService, Mockito.times(1))
            .calculate();
    }
}
