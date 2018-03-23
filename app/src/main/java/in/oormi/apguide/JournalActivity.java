package in.oormi.apguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        tvMsg.setText(String.valueOf(totalPages) + getString(R.string.jpages));

        editPage = (EditText)findViewById(R.id.editTextPage);
        editPage.setGravity(Gravity.START);
        editPage.setTextColor(Color.rgb(0,0,200));

        newPage();

        final ImageButton mbuttonJournalNew =
                (ImageButton) findViewById(R.id.imageButtonJournalNew);
        mbuttonJournalNew.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        newPage();
                        mbuttonJournalNew.startAnimation(AnimationUtils.
                                loadAnimation(JournalActivity.this, R.anim.buttonpress));
                    }
                });


        final ImageButton mbuttonJournalNext =
                (ImageButton) findViewById(R.id.imageButtonJournalNext);
        mbuttonJournalNext.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        nPage++;
                        showPage();
                        mbuttonJournalNext.startAnimation(AnimationUtils.
                                loadAnimation(JournalActivity.this, R.anim.buttonpress));
                    }
                });

        final ImageButton mbuttonJournalPrevious =
                (ImageButton) findViewById(R.id.imageButtonJournalPrevious);
        mbuttonJournalPrevious.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        nPage--;
                        showPage();
                        mbuttonJournalPrevious.startAnimation(AnimationUtils.
                                loadAnimation(JournalActivity.this, R.anim.buttonpress));
                    }
                });

        final ImageButton mbuttonJournalDelete =
                (ImageButton) findViewById(R.id.imageButtonJournalDelete);
        mbuttonJournalDelete.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        delPage();
                        mbuttonJournalDelete.startAnimation(AnimationUtils.
                                loadAnimation(JournalActivity.this, R.anim.buttonpress));
                    }
                });

//        mbuttonJournalNew.setColorFilter(Color.argb(50, 255, 0, 200));
//        mbuttonJournalNext.setColorFilter(Color.argb(50, 255, 0, 200));
//        mbuttonJournalPrevious.setColorFilter(Color.argb(50, 255, 0, 200));
//        mbuttonJournalDelete.setColorFilter(Color.argb(50, 255, 0, 200));

    }

    private void newPage() {
        savePage();
        journalDate = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date());
        newText = getString(R.string.jtxt1) + journalDate + getString(R.string.jtxt2);
        editPage.setText(newText);
        editPage.setSelection(editPage.getText().length()-15);
        isNewPage = true;
        nPage = totalPages + 1;
        tvMsg.setText(getString(R.string.jnew) + String.valueOf(totalPages));
        editPage.setEnabled(isNewPage);
    }

    private void showPage() {
        savePage();
        if (totalPages<1) {
            newPage();
            return;
        }
        if (nPage<1) nPage = 1;
        if (nPage>totalPages) nPage = totalPages;
        editPage.setText(jdb.getPage(nPage));
        isNewPage = false;
        tvMsg.setText("Page " + String.valueOf(nPage) + " of " + totalPages);
        editPage.setEnabled(isNewPage);
    }

    private void delPage() {
        if (isNewPage) {
            newPage();
            return;
        }

        jdb.deletePage(nPage);
        totalPages = jdb.getPageCount();
        nPage++;
        showPage();
        tvMsg.setText(getString(R.string.jdeleted) + String.valueOf(nPage) + " of " + totalPages);
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
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                getString(R.string.jentrysharetitle) + journalDate);
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
                                    tvMsg.setText(getString(R.string.jcleared)
                                            + String.valueOf(totalPages)
                                            + getString(R.string.jpages));
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
                builder.setTitle(R.string.confirmreset);
                builder.setMessage(R.string.resetmsg)
                        .setPositiveButton(R.string.resetyes, dialogClickListener)
                        .setNegativeButton(R.string.resetno, dialogClickListener).show();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

}
