package ru.practicum.shareit.comment;

public class CommentMapper {
    public static CommentDto toDto(Comment c, String authorName) {
        return CommentDto.builder()
                .id(c.getId())
                .text(c.getText())
                .authorName(authorName)
                .created(c.getCreated()).build();
    }
}
