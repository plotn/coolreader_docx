package org.coolreader.docx;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.HtmlExporterNonXSLT;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.LoadFromZipNG;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBException;


public class MainActivity extends AppCompatActivity {

    EditText edConvertMessage;
    private LayoutInflater mInflater = null;

    private String convertDocxFile(String sFile, String sPath) throws org.docx4j.openpackaging.exceptions.Docx4JException,
            java.io.IOException {
        System.out.println("about to create package");
        Docx4jProperties.setProperty("docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir",
                this.getExternalCacheDir()+"/tempFont");
        // org.apache.harmony.xml.parsers.SAXParserFactoryImpl throws SAXNotRecognizedException
        // for feature http://javax.xml.XMLConstants/feature/secure-processing
        // so either disable XML security, or use a different parser.  Here we disable it.
        org.docx4j.jaxb.ProviderProperties.getProviderProperties().put(JAXBContextImpl.DISABLE_XML_SECURITY, Boolean.TRUE);
        // Can we init the Context?
        // You can delete this if you want...
        System.out.println(Context.getJaxbImplementation());
        System.out.println(Context.jc.getClass().getName());

        File f = new File(sFile);
        File directory = new File(sPath);
        if(!directory.exists()){
            directory.mkdir();
        }
        String sNewName = sPath + f.getName()+".html";
        WordprocessingMLPackage w = WordprocessingMLPackage.load(f);
        String IMAGE_DIR_NAME = sPath + f.getName()+"_images";
        String baseURL = f.getName()+"_images/";
        ConversionImageHandler conversionImageHandler = new CoolReaderConversionImageHandler( IMAGE_DIR_NAME, // <-- don't use a path separator here
                baseURL, false);
        HtmlExporterNonXSLT withoutXSLT = new HtmlExporterNonXSLT(w, conversionImageHandler);
        FileOutputStream fos=new FileOutputStream(sNewName);
        XmlUtils.w3CDomNodeToOutputStream(withoutXSLT.export(),fos);
        fos.close();
        return sNewName;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Toast toast = Toast.makeText(MainActivity.this, "New intent!!!",
                Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(this);
        final View view = mInflater.inflate(org.coolreader.docx.R.layout.activity_main, null);
        //setContentView(org.coolreader.docx.R.layout.activity_main);
        setContentView(view);
        Toolbar toolbar = (Toolbar) findViewById(org.coolreader.docx.R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        final String sFile = intent.getStringExtra(Intent.EXTRA_TEXT);
        final String sPath = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        //final String sFile = "/storage/emulated/0/Books/33.docx";
        //final String sPath = "/storage/emulated/0/Books/converted/";

        EditText edInpFile = (EditText)findViewById(R.id.inp_file);
        edInpFile.setKeyListener(null);  //setInputType(InputType.TYPE_NULL);
        EditText edSaveTo = (EditText)findViewById(R.id.save_to);
        edSaveTo.setKeyListener(null); //setInputType(InputType.TYPE_NULL);

        edConvertMessage = (EditText)findViewById(R.id.convert_message);
        edConvertMessage.setKeyListener(null); //.setInputType(InputType.TYPE_NULL);

        edInpFile.setText(sFile);
        edSaveTo.setText(sPath);

        if (sFile == null) {
            edConvertMessage.setText("This application should not be run standalone, it should be called from within CoolReader");
        } else {
            edConvertMessage.setText("Converting document...");

            final String sText = "Done!";

            new Thread(new Runnable() {
                public void run() {
                    String sText1 = sText;
                    boolean bErr = false;
                    String sNewName = "";
                    try {
                        sNewName = convertDocxFile(sFile, sPath);
                    } catch (Exception e) {
                        sText1 = "There was an error during conversion:\n\n"+e.getMessage()+"\n\n"+e.getStackTrace().toString();
                        bErr = true;
                    }
                    final String sText2 = sText1;
                    final boolean bErr2 = bErr;
                    final File fNewName = new File(sNewName);
                    final String sNewName2 = sNewName;
                    view.post(new Runnable() {
                        public void run() {
                            edConvertMessage.setText(sText2);
                            if (!bErr2) {
//                                Toast toast = Toast.makeText(MainActivity.this, "Successful convertion! Click on the file in CoolReader once again.",
//                                        Toast.LENGTH_LONG);
//                                toast.show();

//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                //intent.setPackage("org.coolreader");
//                                Uri uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID, fNewName);
//                                intent.setDataAndType(ur i, "text/html");
//                                startActivity(intent);

                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setPackage("org.coolreader");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setType("text/plain");
                                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, sNewName2);
                                try {
                                    MainActivity.this.startActivity(intent);
                                    finish();
                                } catch (ActivityNotFoundException e) {
                                    Toast toast2 = Toast.makeText(MainActivity.this, "Cannot find CoolReader activity",
                                            Toast.LENGTH_LONG);
                                    toast2.show();
                                } catch (Exception e) {
                                    Toast toast2 = Toast.makeText(MainActivity.this, "exception while open org.coolreader",
                                            Toast.LENGTH_LONG);
                                    toast2.show();
                                }
                            }
                        }
                    });
                }
            }).start();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(org.coolreader.docx.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, sFile, Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(org.coolreader.docx.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == org.coolreader.docx.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
