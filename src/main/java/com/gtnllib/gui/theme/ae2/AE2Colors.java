package com.gtnllib.gui.theme.ae2;

import com.gtnllib.gui.theme.ae2.AE2ButtonDrawable.State;

/**
 * Semantic ARGB colors for one AE2 variant.
 *
 * @param text         primary text color
 * @param muted        muted text color
 * @param secondary    secondary text color
 * @param buttonText   normal button text color
 * @param hoverText    highlighted button text color
 * @param disabledText disabled button text color
 * @param fieldText    text-field content color
 * @param placeholder  text-field hint color
 * @param line         separator and grid-line color
 * @param edge         panel edge color
 * @param panel        panel fill color
 * @param well         inset fill color
 * @param outline      outline color
 * @param raised       raised surface color
 * @param good         positive status color
 * @param bad          negative status color
 * @param info         informational status color
 * @param warning      warning status color
 * @param error        input validation error color
 */
public record AE2Colors(int text, int muted, int secondary, int buttonText, int hoverText, int disabledText,
    int fieldText, int placeholder, int line, int edge, int panel, int well, int outline, int raised, int good, int bad,
    int info, int warning, int error) {

    /**
     * Resolves button text color for a visual state.
     *
     * @param state button state
     * @return ARGB text color
     */
    public int button(State state) {
        return switch (state) {
            case NORMAL -> buttonText;
            case HOVER -> hoverText;
            case DISABLED -> disabledText;
        };
    }

    /**
     * Starts a palette filled in by name, so it does not depend on the order of the nineteen components. A color left
     * unset is 0, fully transparent.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Collects the colors of an {@link AE2Colors} by name; each setter takes an ARGB color. */
    public static class Builder {

        private int text, muted, secondary, buttonText, hoverText, disabledText, fieldText, placeholder, line, edge,
            panel, well, outline, raised, good, bad, info, warning, error;

        /**
         * @param text primary text color
         * @return this builder
         */
        public Builder text(int text) {
            this.text = text;
            return this;
        }

        /**
         * @param muted muted text color
         * @return this builder
         */
        public Builder muted(int muted) {
            this.muted = muted;
            return this;
        }

        /**
         * @param secondary secondary text color
         * @return this builder
         */
        public Builder secondary(int secondary) {
            this.secondary = secondary;
            return this;
        }

        /**
         * @param buttonText normal button text color
         * @return this builder
         */
        public Builder buttonText(int buttonText) {
            this.buttonText = buttonText;
            return this;
        }

        /**
         * @param hoverText highlighted button text color
         * @return this builder
         */
        public Builder hoverText(int hoverText) {
            this.hoverText = hoverText;
            return this;
        }

        /**
         * @param disabledText disabled button text color
         * @return this builder
         */
        public Builder disabledText(int disabledText) {
            this.disabledText = disabledText;
            return this;
        }

        /**
         * @param fieldText text-field content color
         * @return this builder
         */
        public Builder fieldText(int fieldText) {
            this.fieldText = fieldText;
            return this;
        }

        /**
         * @param placeholder text-field hint color
         * @return this builder
         */
        public Builder placeholder(int placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * @param line separator and grid-line color
         * @return this builder
         */
        public Builder line(int line) {
            this.line = line;
            return this;
        }

        /**
         * @param edge panel edge color
         * @return this builder
         */
        public Builder edge(int edge) {
            this.edge = edge;
            return this;
        }

        /**
         * @param panel panel fill color
         * @return this builder
         */
        public Builder panel(int panel) {
            this.panel = panel;
            return this;
        }

        /**
         * @param well inset fill color
         * @return this builder
         */
        public Builder well(int well) {
            this.well = well;
            return this;
        }

        /**
         * @param outline outline color
         * @return this builder
         */
        public Builder outline(int outline) {
            this.outline = outline;
            return this;
        }

        /**
         * @param raised raised surface color
         * @return this builder
         */
        public Builder raised(int raised) {
            this.raised = raised;
            return this;
        }

        /**
         * @param good positive status color
         * @return this builder
         */
        public Builder good(int good) {
            this.good = good;
            return this;
        }

        /**
         * @param bad negative status color
         * @return this builder
         */
        public Builder bad(int bad) {
            this.bad = bad;
            return this;
        }

        /**
         * @param info informational status color
         * @return this builder
         */
        public Builder info(int info) {
            this.info = info;
            return this;
        }

        /**
         * @param warning warning status color
         * @return this builder
         */
        public Builder warning(int warning) {
            this.warning = warning;
            return this;
        }

        /**
         * @param error input validation error color
         * @return this builder
         */
        public Builder error(int error) {
            this.error = error;
            return this;
        }

        /**
         * Creates the colors.
         *
         * @return the filled-in colors
         */
        public AE2Colors build() {
            return new AE2Colors(
                text,
                muted,
                secondary,
                buttonText,
                hoverText,
                disabledText,
                fieldText,
                placeholder,
                line,
                edge,
                panel,
                well,
                outline,
                raised,
                good,
                bad,
                info,
                warning,
                error);
        }
    }
}
