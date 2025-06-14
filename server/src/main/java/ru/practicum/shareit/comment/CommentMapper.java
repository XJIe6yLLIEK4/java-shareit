package ru.practicum.shareit.comment;

public class CommentMapper {
    public static CommentDto toDto(Comment c) {
        return CommentDto.builder()
                .id(c.getId())
                .text(c.getText())
                .authorName(c.getAuthor().getName())
                .created(c.getCreated()).build();
    }
}
