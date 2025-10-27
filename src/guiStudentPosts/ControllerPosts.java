package guiStudentPosts;

import entityClasses.*;

/**
 * The ControllerPosts class serves as the controller layer in the MVC structure 
 * for managing posts within the user interface.
 * 
 * It connects the ModelPosts (data and logic) with the ViewPosts (user interface),
 * handling user actions such as opening discussion threads and ensuring the 
 * current user context is passed throughout the interaction.
 */
public class ControllerPosts {

    // ---------------------- Fields ----------------------
    // The model instance handles post data and logic.
    // The currentUser represents the active user interacting with posts.
    private final ModelPosts model;
    private final User currentUser;

    // ---------------------- Constructor ----------------------

    /**
     * Initializes the controller by creating a new ModelPosts instance 
     * and setting the currently active user.
     * 
     * @param currentUser the user currently using the system
     */
    public ControllerPosts(User currentUser) {
        this.model = new ModelPosts();
        this.currentUser = currentUser;
    }

    // ---------------------- Methods ----------------------

    /**
     * Launches the discussion thread interface in a new stage (window).
     * This method connects the controller and model to the corresponding view.
     */
    public void openDiscussionThread() {
        new ViewPosts(currentUser, model);
    }
}
