package ru.practicum.shareit.exception;

public class ExceptionSameEmail extends RuntimeException {

    public ExceptionSameEmail() {
        super("Данный email уже зарегистрирован");
    }

    public ExceptionSameEmail(String message) {
        super(message);
    }
}
