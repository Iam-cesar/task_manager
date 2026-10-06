package com.example.task_manager.exceptions;

public class LabelNotFoundException extends RuntimeException {
    public LabelNotFoundException () { super("Label not found"); }

    public LabelNotFoundException (final String message) { super(message); }

    public LabelNotFoundException (final String message, final Throwable cause) { super(message, cause); }
}
