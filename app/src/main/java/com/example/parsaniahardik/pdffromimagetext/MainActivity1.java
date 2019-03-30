package com.example.parsaniahardik.pdffromimagetext;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

//import org.w3c.dom.Document;
public class MainActivity1 extends AppCompatActivity {

    private Button btn, btnScroll;
    private LinearLayout llPdf;
    private Bitmap bitmap;
    ProgressDialog progressDialog;

    RecyclerAdapter recyclerAdapter;
    ArrayList<String> arrayList = new ArrayList<>();
    RecyclerView recyclerview;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn = findViewById(R.id.btn);
        llPdf = findViewById(R.id.llPdf);

        recyclerview = findViewById(R.id.recyclerview);

        for (int i = 0; i < 50; i++) {
            arrayList.add("Bank" + i);
        }


        setAdapter();


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new convertViewtoPdfToBitmapAsyncTask().execute();

            }
        });

    }


    void setAdapter() {
        recyclerAdapter = new RecyclerAdapter(MainActivity1.this, arrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(recyclerAdapter);

    }


    private class convertViewtoPdfToBitmapAsyncTask extends AsyncTask<String, Bitmap, Bitmap> {
        File file = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity1.this,
                    "Converting Files",
                    "Please wait...");
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            RecyclerView.Adapter adapter = recyclerview.getAdapter();
            Bitmap bigBitmap = null;
            if (adapter != null) {
                int size = adapter.getItemCount();
                int height = 0;
                Paint paint = new Paint();
                int iHeight = 0;
                final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

                // Use 1/8th of the available memory for this memory cache.
                final int cacheSize = maxMemory / 8;
                LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
                for (int i = 0; i < size; i++) {
                    RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerview, adapter.getItemViewType(i));
                    adapter.onBindViewHolder(holder, i);
                    holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(recyclerview.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                    holder.itemView.setDrawingCacheEnabled(true);
                    holder.itemView.buildDrawingCache();
                    Bitmap drawingCache = holder.itemView.getDrawingCache();
                    if (drawingCache != null) {
                        bitmaCache.put(String.valueOf(i), drawingCache);
                    }

                    height += holder.itemView.getMeasuredHeight();
                }

                bigBitmap = Bitmap.createBitmap(recyclerview.getMeasuredWidth(), 500, Bitmap.Config.ARGB_8888);
                Canvas bigCanvas = new Canvas(bigBitmap);
                bigCanvas.drawColor(Color.WHITE);

                Document document = new Document(PageSize.A4);
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "samples.pdf");

                try {
                    PdfWriter.getInstance(document, new FileOutputStream(file));
                } catch (DocumentException | FileNotFoundException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < size; i++) {
                    try {
                        //Adding the content to the document
                        Bitmap bmp = bitmaCache.get(String.valueOf(i));
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        Image image = Image.getInstance(stream.toByteArray());
                        image.scalePercent(70);
                        image.setAlignment(Image.MIDDLE);
                        if (!document.isOpen()) {
                            document.open();
                        }
                        document.add(image);

                    } catch (Exception ex) {
                        Log.e("TAG-ORDER PRINT ERROR", ex.getMessage());
                    }
                }
                if (document.isOpen()) {
                    document.close();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap resultBitmap) {
            super.onPostExecute(resultBitmap);
            progressDialog.dismiss();

            Toast.makeText(MainActivity1.this, "PDF File Generated Successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }

}
