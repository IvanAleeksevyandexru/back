package ru.gosuslugi.pgu.fs.descriptor;


import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;

public interface ErrorModalDescriptorService {

    ErrorModalWindow getErrorModal(ErrorModalView view);
}
