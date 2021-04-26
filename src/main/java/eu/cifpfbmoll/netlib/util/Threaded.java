package eu.cifpfbmoll.netlib.util;

/**
 * The Threaded class encapsulates the necessary functionality to
 * start, pause, resume, stop and join a thread.
 *
 * <p>The Thread is initialized and started automatically when
 * {@link Threaded#start()} is called.</p>
 *
 * <p>A Class extending Threaded must implement {@link Runnable#run()}
 * like a normal {@link Runnable} would. Additionally, Threaded has
 * a few boolean attributes which must be used to control the thread execution:</p>
 *
 * <ul>
 *     <li>run: indicates if the thread should execute <code>while (this.run) {}</code>.</li>
 *     <li>paused: indicates if the thread should be paused <code>if (this.paused) {}</code>.</li>
 * </ul>
 *
 * <p>It is up to the implementation to check for the state of these variables.</p>
 */
public abstract class Threaded implements Runnable {
    private Thread thread = null;
    protected volatile boolean run = false;
    protected volatile boolean paused = false;

    /**
     * If the thread is not created initialize it,
     * resume execution otherwise.
     *
     * @see Threaded#resume()
     */
    public void start() {
        if (this.thread == null) {
            this.thread = new Thread(this, getClass().getSimpleName());
            this.run = true;
            this.thread.start();
        } else {
            resume();
        }
    }

    /**
     * Pause the execution of the thread.
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Resume the execution of the thread.
     */
    public void resume() {
        this.paused = false;
    }

    /**
     * Wait for the thread to end.
     *
     * @return true if there were no errors, false otherwise
     */
    public boolean join() {
        return join(-1);
    }

    /**
     * Wait for the thread to end with a timeout.
     *
     * @param millis timeout in milliseconds
     * @return true if there were no errors, false otherwise
     */
    public boolean join(int millis) {
        if (this.thread == null) return false;
        try {
            if (millis < 0)
                this.thread.join();
            else
                this.thread.join(millis);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Stop the execution of the thread.
     */
    public void stop() {
        this.run = false;
        this.thread = null;
    }
}
