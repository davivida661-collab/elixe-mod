package me.zero.alpine.event.type;

/**
 * Implementation of {@link ICancellable}
 *
 * @author Brady
 * @since 2/10/2017
 */
public class Cancellable implements ICancellable {

    /**
     * Cancelled state
     */
    private boolean cancelled;

    
    public void cancel() {
        this.cancelled = true;
    }

    
    public boolean isCancelled() {
        return this.cancelled;
    }
}
