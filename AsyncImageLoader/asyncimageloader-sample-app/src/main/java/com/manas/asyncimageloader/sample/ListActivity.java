package com.manas.asyncimageloader.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.manas.asyncimageloader.AsyncImageLoader;
import com.manas.asyncimageloader.ImageDimensions;


/**
 * Created by Manas on 9/6/2014.
 */
public class ListActivity extends Activity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new CustomListAdapter());
    }

    private class CustomListAdapter extends BaseAdapter implements AbsListView.OnItemClickListener {

        public CustomListAdapter() {
            listView.setOnItemClickListener(this);
        }

        String[] imageUrls = new String[] {
                "http://cache2.allpostersimages.com/p/LRG/26/2686/DBWUD00Z/posters/blair-james-p-scenic-view-of-the-rocky-coastline-near-peggys-cove.jpg",
                "http://cache2.artprintimages.com/lrg/26/2688/1KLUD00Z.jpg",
                "http://cruises.about.com/library/graphics/visitorspics/photo%209.JPG",
                "http://bilder.poster.net/LRG/27/2706/OM1ND00Z.jpg",
                "http://www.movieposterskey.com/postersimages/scenic-view-of-villa-rufolo-terrace-gardens-and-wagner-terrace.jpg",
                "http://static.freepik.com/free-photo/nature-scene--nature--view_19-125108.jpg",
                "http://imagesus.homeaway.com/mda01/e5ffbb58-d635-4d67-9f9d-0c6212478b45.1.12",
                "http://4.bp.blogspot.com/_koVqpw2OuKQ/SNJSVIyPSeI/AAAAAAAACOA/oCW-uZ26yW4/s400/Scenic+View.jpg",
                "http://imagecache5.art.com/LRG/26/2685/4SXUD00Z.jpg",
                "http://imagesus.homeaway.com/mda01/a6c586b4-0d54-435c-b631-0c4a9be889b3.1.6",
                "http://p.rdcpix.com/v03/l57bd4944-m0xd-w400_h300_q80.jpg",
                "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSGsqg77fP5AzZZze5RGG8RBQzt4SIvikklxSQTdiFpvTO8g5ju",
                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTy8BnGjLIc_5rZ41fEaz_7TEyfLh3DHpI5oN5J2hCsrzPyJgbp",
                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRmsiPlKYI4h2SmwgfwDyxYsCg7s0T5-uxwS7aasNIF2EjZWnK_",
                "http://4.bp.blogspot.com/_miGBtNTDMT0/SrNdtJxaUNI/AAAAAAAADEs/J9LAhp0-imE/s400/Round+Bales+in+Vermont.jpg",
                "http://traveltips.usatoday.com/DM-Resize/photos.demandstudios.com/getty/article/171/48/87535062_XS.jpg?w=560&h=560&keep_ratio=1&webp=1",
                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSjLajjjCe0px9i24PAGbwCpFr_j2AIAfIJVNiZl9UZvWFV2M6vGg",
                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRRBzpttVWnAfsRXH1aPK_yceQ9SGAhpEUQ7axvGXMm2uufeWt2",
                "http://4.bp.blogspot.com/_XCX5t6ypRfQ/TPO1IdASp-I/AAAAAAAAA1Y/mbGPbIfMINw/s400/Khunjrab_Pass.jpg",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSqox8fTvWAB2jtTtk5Ckw-HR3WQvCd8svc7-68tIOdvulwVO0gvA",
                "http://3.bp.blogspot.com/-ijtqWKSeOMU/T_zvJROMUdI/AAAAAAAAIro/Dt3QT-jv-Vw/s400/100_4312.JPG"
        };

        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public Object getItem(int i) {
            return imageUrls[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int pos, View convertView, ViewGroup container) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                row = getLayoutInflater().inflate(R.layout.listview_item, container, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolder) row.getTag();
            }

            AsyncImageLoader.getInstance().displayImage(holder.imageView, imageUrls[pos], new ImageDimensions(400f, 400f), null, null);
            holder.textView.setText("Image " + (pos+1));

            return row;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            Intent intent = new Intent(ListActivity.this, ViewPagerActivity.class);
            intent.putExtra("pos", pos);
            intent.putExtra("imageUrls", imageUrls);
            startActivity(intent);
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(View row) {
            imageView = (ImageView) row.findViewById(R.id.imageView);
            textView = (TextView) row.findViewById(R.id.textView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_cache) {
            AsyncImageLoader.getInstance().clearCache();
            Toast.makeText(this, "Cache successfully cleared", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
