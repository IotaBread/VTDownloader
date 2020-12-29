package io.github.bymartrixx.vtd.gui.widget;

import io.github.bymartrixx.vtd.gui.VTDScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DownloadButtonWidget extends ButtonWidget {
    /**
     * Keeps track on how much time left there is for the success/failure message
     */
    public int messageTicks = 0;
    /**
     * The message to show if {@link #success} is true
     */
    private final Text successMessage;
    /**
     * The message to show if {@link #success} is false
     */
    private final Text failureMessage;
    private boolean success = false;

    /**
     * @param x              the horizontal position of the button.
     * @param y              the vertical position of the button
     * @param width          the width of the button.
     * @param height         the height of the button.
     * @param message        the message to show if {@link #messageTicks} is 0.
     * @param successMessage the message to show if the download was successful.
     * @param failureMessage the message to show if the download was not successful.
     * @param onPress        what to execute when the button is pressed.
     */
    public DownloadButtonWidget(int x, int y, int width, int height, Text message, Text successMessage, Text failureMessage, PressAction onPress) {
        super(x, y, width, height, message, onPress);
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }

    public Text getMessage() {
        if (this.messageTicks == 0) {
            return super.getMessage();
        } else {
            return this.success ? this.successMessage : this.failureMessage;
        }
    }

    /**
     * Set if the download was successful or not. Use {@link #setSuccess(boolean, int)} to set the message ticks other than 80.
     *
     * @param success the result of the download. true for success, false for failure.
     */
    public void setSuccess(boolean success) {
        this.setSuccess(success, 80);
    }

    /**
     * Set if the download was successful or not.
     *
     * @param success      the result of the download. true for success, false for failure.
     * @param messageTicks the number of ticks the success/failure message will be shown. Default is 80
     */
    public void setSuccess(boolean success, int messageTicks) {
        this.success = success;
        this.messageTicks = messageTicks;
    }

    public void tick() {
        if (this.messageTicks > 0) {
            --this.messageTicks;
            if (this.messageTicks == 0)
                VTDScreen.getInstance().resetDownloadProgress();
        }
    }
}
