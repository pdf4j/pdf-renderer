package com.atlassian.pdfview.colorspace;

import java.awt.color.ICC_Profile;
import java.io.IOException;

public class DefaultICCProfile
{
    public static ICC_Profile getDefaultIccProfile() throws IOException
    {
        return ICC_Profile.getInstance(ICC_Profile.class.getResourceAsStream("/com/atlassian/pdfview/colorspace/Generic_CMYK_Profile.icc"));
    }

}
