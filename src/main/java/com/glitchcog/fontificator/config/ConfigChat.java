package com.glitchcog.fontificator.config;

import java.awt.Rectangle;
import java.util.List;
import java.util.Properties;

/**
 * The Configuration for the Chat Window
 * 
 * @author Matt Yanos
 */
public class ConfigChat extends Config
{
    public static final int MIN_CHROMA_CORNER_RADIUS = 0;
    public static final int MAX_CHROMA_CORNER_RADIUS = 128;

    private Boolean scrollable;

    private Boolean resizable;

    private Integer width;

    private Integer height;

    private Boolean chromaEnabled;

    private Boolean chromaInvert;

    /**
     * Used to represent the border, so the x and y are the left and top borders, and w and h are the right and bottom
     * borders
     */
    private Rectangle chromaBorder;

    private Integer chromaCornerRadius;

    private Boolean alwaysOnTop;

    @Override
    public void reset()
    {
        this.scrollable = null;
        this.resizable = null;
        this.width = null;
        this.height = null;
        this.chromaEnabled = null;
        this.chromaInvert = null;
        this.chromaBorder = null;
        this.chromaCornerRadius = null;
        this.alwaysOnTop = null;
    }

    public List<String> validateDimStrings(List<String> errors, String widthStr, String heightStr)
    {
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_WIDTH, widthStr, 1, errors);
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_HEIGHT, heightStr, 1, errors);
        return errors;
    }

    public List<String> validateChromaDimStrings(List<String> errors, String leftStr, String topStr, String rightStr, String botStr)
    {
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_CHROMA_LEFT, leftStr, 0, errors);
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_CHROMA_TOP, topStr, 0, errors);
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_CHROMA_RIGHT, rightStr, 0, errors);
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_CHROMA_BOTTOM, botStr, 0, errors);
        return errors;
    }

    public List<String> validateStrings(List<String> errors, String widthStr, String heightStr, String chromaLeftStr, String chromaTopStr, String chromaRightStr, String chromaBottomStr, String chromaCornerStr, String scrollBool, String resizeBool, String chromaBool, String invertBool, String topBool)
    {
        validateBooleanStrings(errors, scrollBool, resizeBool, chromaBool, invertBool, topBool);

        validateDimStrings(errors, widthStr, heightStr);
        validateChromaDimStrings(errors, chromaLeftStr, chromaTopStr, chromaRightStr, chromaBottomStr);
        validateIntegerWithLimitString(FontificatorProperties.KEY_CHAT_CHROMA_CORNER, chromaCornerStr, 0, errors);

        return errors;
    }

    @Override
    public List<String> load(Properties props, List<String> errors)
    {
        this.props = props;

        reset();

        // Check that the values exist
        baseValidation(props, FontificatorProperties.CHAT_KEYS, errors);

        if (errors.isEmpty())
        {
            final String widthStr = props.getProperty(FontificatorProperties.KEY_CHAT_WIDTH);
            final String heightStr = props.getProperty(FontificatorProperties.KEY_CHAT_HEIGHT);
            final String chromaLeftStr = props.getProperty(FontificatorProperties.KEY_CHAT_CHROMA_LEFT);
            final String chromaTopStr = props.getProperty(FontificatorProperties.KEY_CHAT_CHROMA_TOP);
            final String chromaRightStr = props.getProperty(FontificatorProperties.KEY_CHAT_CHROMA_RIGHT);
            final String chromaBottomStr = props.getProperty(FontificatorProperties.KEY_CHAT_CHROMA_BOTTOM);

            final String chromaCornerStr = props.getProperty(FontificatorProperties.KEY_CHAT_CHROMA_CORNER);

            final String scrollBool = props.getProperty(FontificatorProperties.KEY_CHAT_SCROLL);
            final String resizeBool = props.getProperty(FontificatorProperties.KEY_CHAT_RESIZABLE);
            final String chromaBool = props.getProperty(FontificatorProperties.KEY_CHAT_CHROMA_ENABLED);
            final String invertBool = props.getProperty(FontificatorProperties.KEY_CHAT_INVERT_CHROMA);
            final String topBool = props.getProperty(FontificatorProperties.KEY_CHAT_ALWAYS_ON_TOP);

            // Check that the values are valid
            validateStrings(errors, widthStr, heightStr, chromaLeftStr, chromaTopStr, chromaRightStr, chromaBottomStr, chromaCornerStr, scrollBool, resizeBool, chromaBool, invertBool, topBool);

            // Fill the values
            if (errors.isEmpty())
            {
                width = Integer.parseInt(widthStr);
                height = Integer.parseInt(heightStr);

                chromaCornerRadius = Integer.parseInt(chromaCornerStr);

                int left = Integer.parseInt(chromaLeftStr);
                int top = Integer.parseInt(chromaTopStr);
                int right = Integer.parseInt(chromaRightStr);
                int bot = Integer.parseInt(chromaBottomStr);
                setChromaBorder(left, top, right, bot);

                scrollable = evaluateBooleanString(props, FontificatorProperties.KEY_CHAT_SCROLL, errors);
                resizable = evaluateBooleanString(props, FontificatorProperties.KEY_CHAT_RESIZABLE, errors);
                chromaEnabled = evaluateBooleanString(props, FontificatorProperties.KEY_CHAT_CHROMA_ENABLED, errors);
                chromaInvert = evaluateBooleanString(props, FontificatorProperties.KEY_CHAT_INVERT_CHROMA, errors);
                alwaysOnTop = evaluateBooleanString(props, FontificatorProperties.KEY_CHAT_ALWAYS_ON_TOP, errors);
            }
        }

        return errors;
    }

    public boolean isScrollable()
    {
        return scrollable;
    }

    public void setScrollable(boolean scrollable)
    {
        this.scrollable = scrollable;
        props.setProperty(FontificatorProperties.KEY_CHAT_SCROLL, Boolean.toString(scrollable));
    }

    public boolean isResizable()
    {
        return resizable;
    }

    public void setResizable(boolean resizable)
    {
        this.resizable = resizable;
        props.setProperty(FontificatorProperties.KEY_CHAT_RESIZABLE, Boolean.toString(resizable));
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
        props.setProperty(FontificatorProperties.KEY_CHAT_WIDTH, Integer.toString(width));
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
        props.setProperty(FontificatorProperties.KEY_CHAT_HEIGHT, Integer.toString(height));
    }

    public boolean isAlwaysOnTop()
    {
        return alwaysOnTop;
    }

    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        this.alwaysOnTop = alwaysOnTop;
        props.setProperty(FontificatorProperties.KEY_CHAT_ALWAYS_ON_TOP, Boolean.toString(alwaysOnTop));
    }

    public Boolean isChromaEnabled()
    {
        return chromaEnabled;
    }

    public void setChromaEnabled(Boolean chromaEnabled)
    {
        this.chromaEnabled = chromaEnabled;
        props.setProperty(FontificatorProperties.KEY_CHAT_CHROMA_ENABLED, Boolean.toString(chromaEnabled));
    }

    public boolean isChromaInvert()
    {
        return chromaInvert;
    }

    public void setChromaInvert(boolean chromaInvert)
    {
        this.chromaInvert = chromaInvert;
        props.setProperty(FontificatorProperties.KEY_CHAT_INVERT_CHROMA, Boolean.toString(chromaInvert));
    }

    public Rectangle getChromaBorder()
    {
        return chromaBorder;
    }

    public void setChromaBorder(int left, int top, int right, int bottom)
    {
        setChromaBorder(new Rectangle(left, top, right, bottom));
    }

    public void setChromaBorder(Rectangle chromaBorder)
    {
        this.chromaBorder = chromaBorder;
        props.setProperty(FontificatorProperties.KEY_CHAT_CHROMA_LEFT, Integer.toString(chromaBorder.x));
        props.setProperty(FontificatorProperties.KEY_CHAT_CHROMA_TOP, Integer.toString(chromaBorder.y));
        props.setProperty(FontificatorProperties.KEY_CHAT_CHROMA_RIGHT, Integer.toString(chromaBorder.width));
        props.setProperty(FontificatorProperties.KEY_CHAT_CHROMA_BOTTOM, Integer.toString(chromaBorder.height));
    }

    public int getChromaCornerRadius()
    {
        return chromaCornerRadius;
    }

    public void setChromaCornerRadius(int chromaCornerRadius)
    {
        this.chromaCornerRadius = chromaCornerRadius;
        props.setProperty(FontificatorProperties.KEY_CHAT_CHROMA_CORNER, Integer.toString(chromaCornerRadius));
    }

}
