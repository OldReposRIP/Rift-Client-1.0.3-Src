package org.yaml.snakeyaml.tokens;

import org.yaml.snakeyaml.error.Mark;

public class CommentToken extends Token {

    public CommentToken(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    public Token.ID getTokenId() {
        return Token.ID.Comment;
    }
}
