package org.fit.pdfdom;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mabb.fontverter.woff.WoffFont;
import org.mabb.fontverter.woff.WoffParser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fit.pdfdom.PDFDomTreeConfig.FontExtractMode.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;

public class TestFonts
{
    @Test
    public void convertPdfWithBareCffFont_outputHtmlHasWoffFontInStyle() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");
        Element style = html.select("style").get(0);

        Assert.assertThat(style.outerHtml(), containsString("@font-face"));
        Assert.assertThat(style.outerHtml(), containsString("x-font-woff"));
    }

    @Test
    public void convertPdfWithBareCffFont_outputHtmlFontIsReadable() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");
        Element style = html.select("style").get(0);

        Matcher matcher = Pattern.compile("x-font-woff;base64,([^']*)'").matcher(style.outerHtml());
        Assert.assertTrue(matcher.find());

        String base64Data = matcher.group(1);
        byte[] fontData = Base64.decodeBase64(base64Data);
        WoffFont font = new WoffParser().parse(fontData);

        Assert.assertThat(font.getTables().size(), greaterThan(1));
    }

    @Test
    public void convertPdfWithBareCffFont_divElementStyleIsUsingFont() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");

        Element div = html.select("div.p").get(0);
        String divStyle = div.attr("style");

        Assert.assertThat(divStyle, containsString("font-family:"));
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void convertPdf_withFontExtractToDirModeSet_thenFontFaceRuleHasUrlToFile() throws Exception
    {
        Document html = convertWithFontSaveToDirMode("/fonts/bare-cff.pdf");
        Element style = html.select("style").get(0);

        Assert.assertThat(style.outerHtml(), containsString("font-extract-dir/EKCFJL+Omsym2.woff"));
    }

    @Test
    public void convertPdf_withFontExtractToDirModeSet_thenFontFileExists() throws Exception
    {
        Document html = convertWithFontSaveToDirMode("/fonts/bare-cff.pdf");
        File tempFontFile = new File(folder.getRoot().getPath() + "/font-extract-dir/EKCFJL+Omsym2.woff");

        Assert.assertTrue(tempFontFile.exists());
    }

    @Test
    public void convertPdf_withIgnoreFontsModeSet_thenNoFontFacesInHtml() throws Exception
    {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setFontMode(IGNORE_FONTS);

        Document html  = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf", config);
        Element style = html.select("style").get(0);

        Assert.assertThat(style.outerHtml(), not(containsString("@font-face")));
    }

    private Document convertWithFontSaveToDirMode(String pdf) throws Exception
    {
        File fontDir = folder.newFolder("font-extract-dir");

        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setFontExtractDirectory(fontDir);
        config.setFontMode(SAVE_TO_DIR);

        return TestUtils.parseWithPdfDomTree(pdf, config);
    }
}
