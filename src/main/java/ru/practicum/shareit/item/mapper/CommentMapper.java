package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorName", source = "user.name")
    @Mapping(target = "id", source = "comment.id")
    CommentDto getCommentDto(Comment comment, User user);

    @Mapping(target = "id", source = "commentDto.id")
    Comment getCommentFromDto(CommentDto commentDto, Item item, User author);

}
