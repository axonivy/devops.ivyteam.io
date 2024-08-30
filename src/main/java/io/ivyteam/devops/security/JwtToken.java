package io.ivyteam.devops.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtToken {

  static PrivateKey get(Path file) throws Exception {
    var keyBytes = Files.readAllBytes(file);
    var spec = new PKCS8EncodedKeySpec(keyBytes);
    var kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }

  public static String createJWT(String githubAppId, long ttlMillis) throws Exception {
    var signatureAlgorithm = SignatureAlgorithm.RS256;

    var nowMillis = System.currentTimeMillis();
    var now = new Date(nowMillis);

    var signingKey = get(Path.of("data", "github-api-app.private-key.der"));

    var builder = Jwts.builder()
        .setIssuedAt(now)
        .setIssuer(githubAppId)
        .signWith(signingKey, signatureAlgorithm);

    if (ttlMillis > 0) {
      var expMillis = nowMillis + ttlMillis;
      var exp = new Date(expMillis);
      builder.setExpiration(exp);
    }
    return builder.compact();
  }
}
