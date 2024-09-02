package io.ivyteam.devops.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import io.jsonwebtoken.Jwts;

public class JwtToken {

  static PrivateKey get(Path file) throws Exception {
    var keyBytes = Files.readAllBytes(file);
    var spec = new PKCS8EncodedKeySpec(keyBytes);
    var kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }

  public static String createJWT(String githubAppId, long ttlMillis) throws Exception {
    var signatureAlgorithm = Jwts.SIG.RS256;

    var nowMillis = System.currentTimeMillis();
    var now = new Date(nowMillis);

    var signingKey = get(Path.of("data", "github-api-app.private-key.der"));

    var builder = Jwts.builder()
        .issuedAt(now)
        .issuer(githubAppId)
        .signWith(signingKey, signatureAlgorithm);

    var expMillis = nowMillis + ttlMillis;
    var exp = new Date(expMillis);
    builder.expiration(exp);

    return builder.compact();
  }
}
