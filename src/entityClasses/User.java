package entityClasses;


import applicationMain.FoundationsMain;
import database.Database;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> Title: User Class </p>
 * 
 * <p> Description: 
 * The {@code User} class represents a user entity in the system. It stores
 * all key information about the user, including name, account credentials,
 * email, and assigned roles (Admin, Student, Staff). 
 * 
 * This class serves as the core identity object used across the application
 * by posts, replies, and database records. </p>
 * 
 * <p> Copyright:
 * Lynn Robert Carter © 2025 </p>
 * 
 * * @author Efe Utku Ural
 * @version 2.00 — 2025-10-26 — Updated for database compatibility and Javadoc compliance
 */
public class User {
	
	// ---------------------- Attributes ----------------------

    /** The unique username for this user’s account. */
    private String userName;

    /** The password associated with this user’s account. */
    private String password;

    /** The user’s legal first name. */
    private String firstName;

    /** The user’s middle name (optional). */
    private String middleName;

    /** The user’s legal last name. */
    private String lastName;

    /** The user’s preferred first name (nickname or display name). */
    private String preferredFirstName;

    /** The user’s primary email address. */
    private String emailAddress;

    /** Indicates whether this user has administrative privileges. */
    private boolean adminRole;

    /** Indicates whether this user is registered as a student. */
    private boolean student;

    /** Indicates whether this user is registered as a staff member. */
    private boolean staff;


    // ---------------------- Constructors ----------------------

    /**
     * Default constructor. Not commonly used in the system.
     */
    public User() {}

    /**
     * Constructs a new {@code User} with all details specified.
     *
     * @param userName  the username of the account
     * @param password  the account password
     * @param fn        the user’s first name
     * @param mn        the user’s middle name
     * @param ln        the user’s last name
     * @param pfn       the preferred first name
     * @param ea        the user’s email address
     * @param r1        whether the user has the Admin role
     * @param r2        whether the user has the Student role
     * @param r3        whether the user has the Staff role
     */
    public User(String userName, String password, String fn, String mn, String ln, 
                String pfn, String ea, boolean r1, boolean r2, boolean r3) {
        this.userName = userName;
        this.password = password;
        this.firstName = fn;
        this.middleName = mn;
        this.lastName = ln;
        this.preferredFirstName = pfn;
        this.emailAddress = ea;
        this.adminRole = r1;
        this.student = r2;
        this.staff = r3;
    }

    /**
     * Constructs a minimal {@code User} object with only a username.
     * Useful when reconstructing objects from database relations.
     *
     * @param userName the username for this user
     */
    public User(String userName) {
        this.userName = userName;
    }


    // ---------------------- Role Setters ----------------------

    /**
     * Assigns the Admin role to this user.
     *
     * @param role {@code true} if the user should have the Admin role
     */
    public void setAdminRole(boolean role) {
        this.adminRole = role;
    }

    /**
     * Assigns or removes the Student role for this user.
     *
     * @param role {@code true} if the user is a student
     */
    public void setStudentUser(boolean role) {
        this.student = role;
    }

    /**
     * Assigns or removes the Staff role for this user.
     *
     * @param role {@code true} if the user is staff
     */
    public void setStaffUser(boolean role) {
        this.staff = role;
    }


    // ---------------------- Attribute Getters ----------------------

    /**
     * Returns this user’s username.
     *
     * @return the user’s username
     */
    public String getUserName() { return userName; }

    /**
     * Returns this user’s password.
     *
     * @return the account password
     */
    public String getPassword() { return password; }

    /**
     * Returns the user’s first name.
     *
     * @return the first name
     */
    public String getFirstName() { return firstName; }

    /**
     * Returns the user’s middle name.
     *
     * @return the middle name
     */
    public String getMiddleName() { return middleName; }

    /**
     * Returns the user’s last name.
     *
     * @return the last name
     */
    public String getLastName() { return lastName; }

    /**
     * Returns the user’s preferred (display) first name.
     *
     * @return the preferred first name
     */
    public String getPreferredFirstName() { return preferredFirstName; }

    /**
     * Returns the user’s email address.
     *
     * @return the email address
     */
    public String getEmailAddress() { return emailAddress; }


    // ---------------------- Attribute Setters ----------------------

    public void setUserName(String s) { userName = s; }
    public void setPassword(String s) { password = s; }
    public void setFirstName(String s) { firstName = s; }
    public void setMiddleName(String s) { middleName = s; }
    public void setLastName(String s) { lastName = s; }
    public void setPreferredFirstName(String s) { preferredFirstName = s; }
    public void setEmailAddress(String s) { emailAddress = s; }


    // ---------------------- Role Getters ----------------------

    /**
     * Returns whether this user is an administrator.
     *
     * @return {@code true} if Admin role is active; otherwise {@code false}
     */
    public boolean getAdminRole() { return adminRole; }

    /**
     * Returns whether this user has the Student role.
     *
     * @return {@code true} if Student role is active; otherwise {@code false}
     */
    public boolean getNewStudent() { return student; }

    /**
     * Returns whether this user has the Staff role.
     *
     * @return {@code true} if Staff role is active; otherwise {@code false}
     */
    public boolean getNewStaff() { return staff; }


    // ---------------------- Utility ----------------------

    /**
     * Returns the total number of roles assigned to this user.
     * Used for quick role count checks.
     *
     * @return an integer from 0–3 indicating how many roles this user has
     */
    public int getNumRoles() {
        int numRoles = 0;
        if (adminRole) numRoles++;
        if (student) numRoles++;
        if (staff) numRoles++;
        return numRoles;
    }
    
    /**
     * Returns a list of students from the database to user.
     * Used for choosing students.
     *
     * @return a list of students
     */
    public static List<User> loadStudentUsers() {
        List<User> result = new ArrayList<>();

        try {
            Database db = FoundationsMain.database;
            db.connectToDatabase();

            List<String[]> rows = db.getAllStudents();

            for (String[] r : rows) {
                User u = new User(
                        r[0], "",           
                        r[1], "",           
                        r[2],               
                        r[1],               
                        r[3],               
                        r[4].equals("Admin"),
                        r[5].equals("Student"),
                        r[6].equals("Staff")
                );
                result.add(u);
            }

        } catch (Exception e) {
            System.err.println("Error loading students: " + e.getMessage());
        }

        return result;
    }

    

    /**
     * Returns a string representation of the user’s identity.
     *
     * @return formatted user summary
     */
    @Override
    public String toString() {
        return userName + " (" + preferredFirstName + " " + lastName + ")";
    }
}
