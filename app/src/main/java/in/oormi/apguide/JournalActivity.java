package in.oormi.apguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JournalActivity extends AppCompatActivity {
    EditText editPage;
    TextView tvMsg;
    String newText;
    String journalDate;
    int nPage, totalPages;
    boolean saveRequired = false;
    boolean isNewPage = false;
    public JournalDBHandler jdb = new JournalDBHandler(this);
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        tvMsg = (TextView)findViewById(R.id.textViewMesg);
        totalPages = jdb.getPageCount();
        tvMsg.setText(String.valueOf(totalPages) + " Pages");

        editPage = (EditText)findViewById(R.id.editTextPage);
        editPage.setGravity(Gravity.START);
        editPage.setTextColor(Color.rgb(0,0,200));

        newPage();


/*
        editPage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isNewPage) saveRequired = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
*/


        ImageButton mbuttonJournalNew = (ImageButton) findViewById(R.id.imageButtonJournalNew);
        mbuttonJournalNew.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        newPage();
                    }
                });


        ImageButton mbuttonJournalNext = (ImageButton) findViewById(R.id.imageButtonJournalNext);
        mbuttonJournalNext.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        nextPage();
                    }
                });

        ImageButton mbuttonJournalPrevious = (ImageButton) findViewById(R.id.imageButtonJournalPrevious);
        mbuttonJournalPrevious.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        prePage();
                    }
                });

        ImageButton mbuttonJournalDelete = (ImageButton) findViewById(R.id.imageButtonJournalDelete);
        mbuttonJournalDelete.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        delPage();
                    }
                });


    }

    private void newPage() {
        journalDate = new SimpleDateFormat("yyyy/MM/dd hh:mm a").format(new Date());
        newText = "AP Journal Entry\nDate: " + journalDate + "\n\nKeywords: \n\nDescription: ";
        editPage.setText(newText);
        editPage.setSelection(editPage.getText().length()-15);
        editPage.setEnabled(true);
        isNewPage = true;
        nPage = totalPages + 1;
    }

    private void nextPage() {
        savePage();
    }

    private void prePage() {
        savePage();
        nPage--;
        if (nPage<1) nPage = 1;
        editPage.setEnabled(false);
        editPage.setText(jdb.getPage(nPage));
        isNewPage = false;
        tvMsg.setText("Page " + String.valueOf(nPage) + " of " + totalPages);
    }

    private void delPage() {
    }

    protected void onPause()
    {
        savePage();
        super.onPause();
    }

    private void savePage() {
        saveRequired = !editPage.getText().toString().equals(newText);
        if (saveRequired && isNewPage) {
            jdb.addData(journalDate, editPage.getText().toString());
            totalPages = jdb.getPageCount();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.journalmenu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider)  MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, editPage.getText().toString());
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My AP Journal Entry - " + journalDate);
        setShareIntent(shareIntent);
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apjinfo:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    jdb.resetDB();
                                    totalPages = jdb.getPageCount();
                                    tvMsg.setText("Journal Cleared. " + String.valueOf(totalPages) + " Pages");
                                    newPage();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(JournalActivity.this);
                builder.setTitle("Confirm Data Reset");
                builder.setMessage("All pages will be deleted. Are you sure?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                break;
        }

        return true;
    }

}
