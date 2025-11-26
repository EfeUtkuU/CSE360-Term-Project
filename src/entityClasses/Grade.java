package entityClasses;

/**
 * <h2>Class: Grade</h2>
 *
 * <p><strong>Description:</strong><br>
 * The {@code Grade} class represents a grade assigned to a student by a staff
 * member. A grade entry consists of:
 * </p>
 *
 * <ul>
 *   <li>the student's username</li>
 *   <li>the staff grader's username</li>
 *   <li>a numeric grade value (0–100)</li>
 *   <li>feedback text</li>
 * </ul>
 *
 * <p>
 * Replies (chat messages) between the student and staff regarding this grade
 * are stored externally in the {@code GRADE_REPLIES} table and are not part
 * of this class. This object stores only the core grade information.
 * </p>
 *
 * <p>
 * The {@code GradeStore} class uses this object to synchronize with the
 * database.
 * </p>
 *
 * @author 
 *   Efe Utku Ural
 *
 * @version
 *   1.00 — 2025-11-24 — Initial version aligned with project standards
 */
public class Grade {

    // ---------------------- Attributes ----------------------

    /** Unique database ID of this grade entry. */
    private int id;

    /** Username of the student receiving the grade. */
    private String studentId;

    /** Username of the staff member assigning the grade. */
    private String staffId;

    /** Numeric grade value (range 0–100). */
    private int gradeValue;

    /** Feedback message written by the staff grader. */
    private String feedback;


    // ---------------------- Constructors ----------------------

    /**
     * Default constructor.
     * <p>
     * Typically used only when populating objects through reflection or
     * database frameworks.
     * </p>
     */
    public Grade() {}

    /**
     * Constructs a full {@code Grade} object with an existing database ID.
     *
     * @param id         the grade's unique database ID
     * @param studentId  username of the student receiving the grade
     * @param staffId    username of the staff member who assigned the grade
     * @param gradeValue numeric grade value (0–100)
     * @param feedback   feedback text written by the staff member
     */
    public Grade(int id, String studentId, String staffId, int gradeValue, String feedback) {
        this.id = id;
        this.studentId = studentId;
        this.staffId = staffId;
        this.gradeValue = gradeValue;
        this.feedback = feedback;
    }

    /**
     * Constructs a {@code Grade} object for new records that have not yet been
     * assigned a database ID.
     *
     * @param studentId  username of the student receiving the grade
     * @param staffId    username of the staff member who assigned the grade
     * @param gradeValue numeric grade value (0–100)
     * @param feedback   feedback text written by the staff member
     */
    public Grade(String studentId, String staffId, int gradeValue, String feedback) {
        this.studentId = studentId;
        this.staffId = staffId;
        this.gradeValue = gradeValue;
        this.feedback = feedback;
    }


    // ---------------------- Getters ----------------------

    /**
     * Returns the unique database ID.
     *
     * @return the grade's ID
     */
    public int getId() { return id; }

    /**
     * Returns the username of the student who received the grade.
     *
     * @return the student username
     */
    public String getStudentId() { return studentId; }

    /**
     * Returns the username of the staff member who assigned the grade.
     *
     * @return the staff username
     */
    public String getStaffId() { return staffId; }

    /**
     * Returns the numeric grade value.
     *
     * @return the grade (0–100)
     */
    public int getGradeValue() { return gradeValue; }

    /**
     * Returns the feedback text associated with this grade.
     *
     * @return feedback message
     */
    public String getFeedback() { return feedback; }


    // ---------------------- Setters ----------------------

    /**
     * Sets the database ID for this grade entry.
     *
     * @param id new database ID
     */
    public void setId(int id) { this.id = id; }

    /**
     * Sets the student username associated with this grade.
     *
     * @param s the student's username
     */
    public void setStudentId(String s) { this.studentId = s; }

    /**
     * Sets the staff member's username.
     *
     * @param s the staff username
     */
    public void setStaffId(String s) { this.staffId = s; }

    /**
     * Sets the numeric grade value (0–100).
     *
     * @param v new grade value
     */
    public void setGradeValue(int v) { this.gradeValue = v; }

    /**
     * Sets the staff feedback text.
     *
     * @param f the feedback message
     */
    public void setFeedback(String f) { this.feedback = f; }


    // ---------------------- Utility ----------------------

    /**
     * Returns a short descriptive representation of the grade.
     *
     * @return a formatted string containing student and grade value
     */
    @Override
    public String toString() {
        return "Grade[" + studentId + " → " + gradeValue + "]";
    }
}
