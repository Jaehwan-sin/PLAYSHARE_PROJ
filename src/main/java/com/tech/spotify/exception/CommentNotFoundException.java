package com.tech.spotify.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }

    public static CommentNotFoundException commentNotFound() {
        return new CommentNotFoundException("댓글을 찾을 수 없습니다.");
    }

}

