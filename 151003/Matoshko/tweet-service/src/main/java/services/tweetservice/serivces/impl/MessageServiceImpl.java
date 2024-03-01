package services.tweetservice.serivces.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import services.tweetservice.domain.entity.Message;
import services.tweetservice.domain.entity.Tweet;
import services.tweetservice.domain.mapper.MessageListMapper;
import services.tweetservice.domain.mapper.MessageMapper;
import services.tweetservice.domain.request.MessageRequestTo;
import services.tweetservice.domain.response.MessageResponseTo;
import services.tweetservice.exceptions.NoSuchMessageException;
import services.tweetservice.exceptions.NoSuchTweetException;
import services.tweetservice.repositories.MessageRepository;
import services.tweetservice.serivces.MessageService;
import services.tweetservice.serivces.TweetService;

import java.util.List;

@Service
@Transactional
@Validated
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final TweetService tweetService;
    private final MessageMapper messageMapper;
    private final MessageListMapper messageListMapper;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, TweetService tweetService, MessageMapper messageMapper, MessageListMapper messageListMapper) {
        this.messageRepository = messageRepository;
        this.tweetService = tweetService;
        this.messageMapper = messageMapper;
        this.messageListMapper = messageListMapper;
    }

    @Override
    public MessageResponseTo create(MessageRequestTo entity) {
        Tweet tweet = tweetService.findTweetByIdExt(entity.tweetId()).orElseThrow(() -> new NoSuchTweetException(entity.tweetId()));
        Message message = messageMapper.toMessage(entity);
        message.setTweet(tweet);
        return messageMapper.toMessageResponseTo(messageRepository.save(message));
    }

    @Override
    public List<MessageResponseTo> read() {
        return messageListMapper.toMessageResponseToList(messageRepository.findAll());
    }

    @Override
    public MessageResponseTo update(MessageRequestTo entity) {
        if (messageRepository.existsById(entity.id())) {
            Message message = messageMapper.toMessage(entity);
            Tweet tweetRef = tweetService.findTweetByIdExt(message.getTweet().getId()).orElseThrow(() -> new NoSuchTweetException(message.getTweet().getId()));
            message.setTweet(tweetRef);
            return messageMapper.toMessageResponseTo(messageRepository.save(message));
        } else {
            throw new NoSuchMessageException(entity.id());
        }
    }

    @Override
    public void delete(Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
        } else {
            throw new NoSuchMessageException(id);
        }
    }

    @Override
    public MessageResponseTo findMessageById(Long id) {
        Message message = messageRepository.findById(id).orElseThrow(() -> new NoSuchMessageException(id));
        return messageMapper.toMessageResponseTo(message);
    }
}
