
package com.payflow.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordService {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 256;
    private static final int ITERATIONS = 310000;

    private static final String ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final SecureRandom RANDOM =
            new SecureRandom();

    // Generate a secure password hash.
    public static String hashPassword(char[] password) {

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] hash = generateHash(password, salt);

        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    // Verify a password against its stored hash.
    public static boolean verifyPassword(
            char[] password,
            String storedHash) {

        if (password == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");

            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);

            if (iterations < 100000 || iterations > 2000000) {
                return false;
            }

            byte[] salt =
                    Base64.getDecoder().decode(parts[1]);

            byte[] expectedHash =
                    Base64.getDecoder().decode(parts[2]);

            if (salt.length != SALT_LENGTH ||
                    expectedHash.length != HASH_LENGTH / 8) {
                return false;
            }

            byte[] actualHash =
                    generateHash(password, salt, iterations);

            return MessageDigest.isEqual(
                    expectedHash,
                    actualHash
            );

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateHash(
            char[] password,
            byte[] salt) {

        return generateHash(password, salt, ITERATIONS);
    }

    private static byte[] generateHash(
            char[] password,
            byte[] salt,
            int iterations) {

        PBEKeySpec specification = new PBEKeySpec(
                password,
                salt,
                iterations,
                HASH_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(specification).getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Password hashing failed.", e
            );

        } finally {
            specification.clearPassword();
        }
    }
}
