package ru.gosuslugi.pgu.fs.common.exception;

public class NoRightToCreateOrderException extends FormBaseException{

    public NoRightToCreateOrderException(String s) {
        super(s);
    }

    public NoRightToCreateOrderException(){
        super("У вас недостаточно прав для создания черновика заявления.");
    }
}
