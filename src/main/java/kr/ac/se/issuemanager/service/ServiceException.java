package kr.ac.se.issuemanager.service;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}

