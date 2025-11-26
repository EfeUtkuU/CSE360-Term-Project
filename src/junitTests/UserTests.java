package junitTests;

import entityClasses.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class UserTests {

    @Test
    void testSetAndGetUserName() {
        User u = new User();
        u.setUserName("efe");
        assertEquals("efe", u.getUserName());
    }

    @Test
    void testEmptyUserName() {
        User u = new User();
        u.setUserName("");
        assertEquals("", u.getUserName());
    }

    @Test
    void testLongUserName() {
        User u = new User();
        String longName = "a".repeat(200);
        u.setUserName(longName);
        assertEquals(longName, u.getUserName());
    }

    @Test
    void testSetAndGetPassword() {
        User u = new User();
        u.setPassword("12345");
        assertEquals("12345", u.getPassword());
    }

    @Test
    void testEmptyPassword() {
        User u = new User();
        u.setPassword("");
        assertEquals("", u.getPassword());
    }

    @Test
    void testEmailSetAndGet() {
        User u = new User();
        u.setEmailAddress("efe@example.com");
        assertEquals("efe@example.com", u.getEmailAddress());
    }

    @Test
    void testInvalidEmailFormat() {
        User u = new User();
        u.setEmailAddress("abc"); 
        assertEquals("abc", u.getEmailAddress()); 
    }

    @Test
    void testRoleAssignmentStudentOnly() {
        User u = new User();
        u.setStudentUser(true);
        assertTrue(u.getNewStudent());
        assertFalse(u.getAdminRole());
        assertFalse(u.getNewStaff());
    }

    @Test
    void testRoleAssignmentAdminOnly() {
        User u = new User();
        u.setAdminRole(true);
        assertTrue(u.getAdminRole());
    }

    @Test
    void testRoleAssignmentStaffOnly() {
        User u = new User();
        u.setStaffUser(true);
        assertTrue(u.getNewStaff());
    }

    @Test
    void testNumRolesNone() {
        User u = new User();
        assertEquals(0, u.getNumRoles());
    }

    @Test
    void testNumRolesAll() {
        User u = new User();
        u.setAdminRole(true);
        u.setStudentUser(true);
        u.setStaffUser(true);
        assertEquals(3, u.getNumRoles());
    }

    @Test
    void testNumRolesTwo() {
        User u = new User();
        u.setAdminRole(true);
        u.setStudentUser(true);
        assertEquals(2, u.getNumRoles());
    }

    @Test
    void testToString() {
        User u = new User("efe", "123", "Efe", "U", "Ural",
                "Efe", "efe@example.com", false, true, false);

        assertTrue(u.toString().contains("efe"));
        assertTrue(u.toString().contains("Ural"));
    }
}
