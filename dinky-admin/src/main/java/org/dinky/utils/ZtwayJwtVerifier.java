/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dinky.utils;

import org.dinky.data.enums.Status;
import org.dinky.data.exception.AuthException;

import java.security.PublicKey;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.Getter;

/**
 * Verify Webwaytoken with RS256. Signature only; exp is not checked.
 */
public class ZtwayJwtVerifier {

    @Getter
    public static class ZtwayClaims {
        private final String username;
        private final String name;

        public ZtwayClaims(String username, String name) {
            this.username = username;
            this.name = name;
        }
    }

    public static ZtwayClaims verify(String token, String pemPublicKey) throws AuthException {
        if (StrUtil.isBlank(token)) {
            throw new AuthException(Status.ZTWAY_TOKEN_MISSING);
        }
        if (StrUtil.isBlank(pemPublicKey)) {
            throw new AuthException(Status.ZTWAY_TOKEN_INVALID);
        }
        try {
            PublicKey publicKey = parsePublicKey(pemPublicKey);
            JWT jwt = JWT.of(token).setSigner(JWTSignerUtil.rs256(publicKey));
            if (!jwt.verify()) {
                throw new AuthException(Status.ZTWAY_TOKEN_INVALID);
            }
            String username = stringClaim(jwt, "username");
            if (StrUtil.isBlank(username)) {
                throw new AuthException(Status.ZTWAY_USERNAME_MISSING);
            }
            return new ZtwayClaims(username, stringClaim(jwt, "name"));
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(e, Status.ZTWAY_TOKEN_INVALID);
        }
    }

    private static String stringClaim(JWT jwt, String name) {
        Object value = jwt.getPayload(name);
        return value == null ? null : String.valueOf(value);
    }

    private static PublicKey parsePublicKey(String pem) {
        String normalized = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return KeyUtil.generatePublicKey("RSA", Base64.decode(normalized));
    }
}
