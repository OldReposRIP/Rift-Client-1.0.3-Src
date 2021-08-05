package org.yaml.snakeyaml.scanner;

import org.yaml.snakeyaml.tokens.Token;

public interface Scanner {

    boolean checkToken(Token.ID... atoken_id);

    Token peekToken();

    Token getToken();
}
