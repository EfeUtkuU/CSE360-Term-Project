package junitTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import entityClasses.*;
import java.time.LocalDateTime;

public class ReplyTests {

    private Post makePost() {
        User author = new User("efe");
        return new Post(author, "test title", "body content", "tag1,tag2", false, false);
    }

    @Test
    void testReplyConstructorSetsFields() {
        Post p = makePost();
        User u = new User("efe");

        Reply r = new Reply(p, u, "hello", false);

        assertEquals("efe", r.getAuthor());
        assertEquals("hello", r.getBody());
        assertFalse(r.isAnswer());
        assertEquals(p.getDatabaseId(), r.getPostDatabaseId());
        assertNotNull(r.getCreatedAt());
        assertNotNull(r.getUpdatedAt());
    }

    @Test
    void testBodySetterUpdatesTimestamp() throws InterruptedException {
        Post p = makePost();
        User u = new User("efe");

        Reply r = new Reply(p, u, "initial", false);

        LocalDateTime old = r.getUpdatedAt();
        Thread.sleep(5);
        r.setBody("updated");

        assertEquals("updated", r.getBody());
        assertTrue(r.getUpdatedAt().isAfter(old));
    }

    @Test
    void testSetAnswerUpdatesTimestamp() throws InterruptedException {
        Post p = makePost();
        User u = new User("efe");

        Reply r = new Reply(p, u, "initial", false);

        LocalDateTime old = r.getUpdatedAt();
        Thread.sleep(5);
        r.setAnswer(true);

        assertTrue(r.isAnswer());
        assertTrue(r.getUpdatedAt().isAfter(old));
    }

    @Test
    void testDatabaseConstructorLoadsCorrectly() {
        Reply r = new Reply(
                10, 5, "efe",
                "hello", true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        assertEquals(10, r.getDatabaseId());
        assertEquals(5, r.getPostDatabaseId());
        assertEquals("efe", r.getAuthor());
        assertTrue(r.isAnswer());
        assertNotNull(r.getCreatedAt());
        assertNotNull(r.getUpdatedAt());
    }

    @Test
    void testSetDatabaseId() {
        Post p = makePost();
        User u = new User("efe");

        Reply r = new Reply(p, u, "hi", false);
        r.setDatabaseId(100);

        assertEquals(100, r.getDatabaseId());
    }

    @Test
    void testSetPostDatabaseId() {
        Post p = makePost();
        User u = new User("efe");

        Reply r = new Reply(p, u, "hi", false);
        r.setPostDatabaseId(50);

        assertEquals(50, r.getPostDatabaseId());
    }

    @Test
    void testEmptyBodyAllowedButTracked() {
        Post p = makePost();
        User u = new User("efe");

        Reply r = new Reply(p, u, "", false);
        assertEquals("", r.getBody());
    }

    @Test
    void testToStringIncludesInfo() {
        Reply r = new Reply(
                3, 1, "efe", "hello", false,
                LocalDateTime.now(), LocalDateTime.now()
        );

        String s = r.toString();
        assertTrue(s.contains("Reply"));
        assertTrue(s.contains("efe"));
        assertTrue(s.contains("hello"));
    }
}
