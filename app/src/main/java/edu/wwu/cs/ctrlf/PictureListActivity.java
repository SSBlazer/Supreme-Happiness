package edu.wwu.cs.ctrlf;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.View;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Pictures. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PictureDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PictureListActivity extends AppCompatActivity {

    public static final int PERMISSION_STORAGE = 823;
    public static File rootFolder;
    /**
     * Random constant.
     */
    private static final int REQUEST_PICTURE = 37;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Uri outputFileUri;

    // gallery url list
    private ArrayList<CreateList> prepareData(){
        ArrayList<CreateList> theimage = new ArrayList<>();
        File file[] = rootFolder.listFiles();
        if (file==null) {
            return theimage;
        }
        for (int i=0; i < file.length; i++)
        {
            CreateList createList = new CreateList();
            createList.setImage_ID(file[i].getName());
            theimage.add(createList);
        }
        return theimage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start of our code
        setContentView(R.layout.activity_picture_list);
        rootFolder = new File(getFilesDir() + File.separator + "Pictures" + File.separator);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.picture_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CreateList> createLists = prepareData();
        MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter); //end

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(takePhotoOnClick());

        if (findViewById(R.id.picture_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        /*View recyclerView = findViewById(R.id.picture_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);*/
    }
/*
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
    }
*/
    @NonNull
    private View.OnClickListener takePhotoOnClick() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                rootFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator);
                rootFolder.mkdirs();
                outputFileUri = Uri.fromFile(new File(rootFolder, "IMG_" + System.currentTimeMillis() + ".jpg"));

                List<Intent> takeNewImageIntents = new ArrayList<>();
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                for (ResolveInfo res : getPackageManager().queryIntentActivities(captureIntent, 0)) {
                    String packageName = res.activityInfo.packageName;
                    Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    takeNewImageIntents.add(intent);
                }

                Intent pickExistingImageIntent = new Intent();
                pickExistingImageIntent.setType("image/*");
                pickExistingImageIntent.setAction(Intent.ACTION_GET_CONTENT);

                Intent chooser = Intent.createChooser(pickExistingImageIntent, "Select Source");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, takeNewImageIntents.toArray(new Parcelable[0]));
                startActivityForResult(chooser, REQUEST_PICTURE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICTURE && resultCode == RESULT_OK) {

            Uri realUri;
            if (data == null || data.getData() == null) {
                realUri = outputFileUri;
            } else {
                realUri = data.getData();
            }

            outputFileUri = realUri;

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
            }
            Intent showPictureIntent = new Intent(getApplicationContext(), ShowPictureActivity.class);
            showPictureIntent.putExtra(ShowPictureActivity.PICTURE_URI, outputFileUri);
            startActivity(showPictureIntent);
            prepareData();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent showPictureIntent = new Intent(getApplicationContext(), ShowPictureActivity.class);
                showPictureIntent.putExtra(ShowPictureActivity.PICTURE_URI, outputFileUri);
                startActivity(showPictureIntent);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("outputFileUri", outputFileUri.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        outputFileUri = Uri.parse(savedInstanceState.getString("outputFileUri"));
    }

/*
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PictureListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(PictureDetailFragment.ARG_ITEM_ID, item.id);
                    PictureDetailFragment fragment = new PictureDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.picture_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PictureDetailActivity.class);
                    intent.putExtra(PictureDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PictureListActivity parent,
                                      List<DummyContent.DummyItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }*/
}
