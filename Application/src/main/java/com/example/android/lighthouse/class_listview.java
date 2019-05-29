package com.example.android.lighthouse;


import android.app.Activity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

public class class_listview extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] anunciosArr;
    private final Integer[] imageId;
    public class_listview(Activity context, String[] anunciosArray, Integer[] imageId) {
        super(context, R.layout.oferta, anunciosArray);
        this.context = context;
        this.anunciosArr = anunciosArray;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.oferta, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(anunciosArr[position]);

        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}