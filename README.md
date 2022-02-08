# form-backend

## Описание микросервисов

### form-backend
Бэкенд плеера форм. Сервис без состояния, отвечает за:
- формирование черновика в ходе заполнения пользователем заявления, его сохранение в draft-service (сервис с хранилищем Cassandra, лежит в отдельном репозитории);
- вычисление следующего экрана для перехода;
- получение данных о пользователе для отображения на экране;
- валидацию введенных пользователем данных.


### pgu-scenario-player-service
Упрощенный плеер форм, который умеет работать с дескрипторами услуг и не требует авторизации. Нужен для простых сценариев на портале Госуслуг и сторонних порталах.
