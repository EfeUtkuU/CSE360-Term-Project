package entityClasses;

/*******
 * <p><strong>Title:</strong> Grade Class</p>
 *
 * <p><strong>Description:</strong> 
 * The {@code Grade} class represents a student’s assigned grade in the system.
 * A grade is created by a staff member and contains:
 *   - the student's username
 *   - the staff grader's username
 *   - a numeric grade value (0–100)
 *   - feedback text
 *
 * The grade may also have replies (chat messages) stored in GRADE_REPLIES,
 * but those are not stored in this class—this class only stores main grade data.
 *
 * This object is used by GradeStore to sync with the database.
 * </p>
 *
 * @author 
 * @version 1.00 — 2025-11-24 — Initial version aligned with project standards
 */
public class Grade {

    // ---------------------- Attributes ----------------------

    /** Unique database ID of this grade entry. */
    private int id;

    /** Username of the student who receives the grade. */
    private String studentId;

    /** Username of the staff member who assigned the grade. */
    private String staffId;

    /** The numeric grade value (0–100). */
    private int gradeValue;

    /** The feedback text written by the staff member. */
    private String feedback;


    // ---------------------- Constructors ----------------------

    /**
     * Default constructor. Rarely used.
     */
    public Grade() {}

    /**
     * Full constructor with database ID.
     *
     * @param id          the database ID
     * @param studentId   the student’s username
     * @param staffId     the staff member’s username
     * @param gradeValue  numeric grade (0–100)
     * @param feedback    staff feedback text
     */
    public Grade(int id, String studentId, String staffId, int gradeValue, String feedback) {
        this.id = id;
        this.studentId = studentId;
        this.staffId = staffId;
        this.gradeValue = gradeValue;
        this.feedback = feedback;
    }

    /**
     * Constructor for creating new grades before they receive a database ID.
     *
     * @param studentId   the student’s username
     * @param staffId     the staff member’s username
     * @param gradeValue  numeric grade (0–100)
     * @param feedback    staff feedback text
     */
    public Grade(String studentId, String staffId, int gradeValue, String feedback) {
        this.studentId = studentId;
        this.staffId = staffId;
        this.gradeValue = gradeValue;
        this.feedback = feedback;
    }


    // ---------------------- Getters ----------------------

    /** @return the database ID of this grade */
    public int getId() { return id; }

    /** @return the student's username */
    public String getStudentId() { return studentId; }

    /** @return the staff member's username */
    public String getStaffId() { return staffId; }

    /** @return the numeric grade value */
    public int getGradeValue() { return gradeValue; }

    /** @return the feedback text */
    public String getFeedback() { return feedback; }


    // ---------------------- Setters ----------------------

    public void setId(int id) { this.id = id; }
    public void setStudentId(String s) { this.studentId = s; }
    public void setStaffId(String s) { this.staffId = s; }
    public void setGradeValue(int v) { this.gradeValue = v; }
    public void setFeedback(String f) { this.feedback = f; }


    // ---------------------- Utility ----------------------

    @Override
    public String toString() {
        return "Grade[" + studentId + " → " + gradeValue + "]";
    }
}
