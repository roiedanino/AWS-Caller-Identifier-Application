package entities;

import android.media.Image;

public class AndroidContact {

    private String name;
    private String number;
    private int imageIndex;

    public AndroidContact(String name, String number, int imageIndex) {
        this.name = name;
        this.number = number;
        this.imageIndex = imageIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }

  /*  static AndroidContact[] contacts = new AndroidContact[]{
            new AndroidContact("John Fotso","38484800982", R.drawable.man),
            new AndroidContact("Marie Annabelle","774634800982", R.drawable.woman),
            new AndroidContact("Patrick Grit","093799332", R.drawable.man2),
            new AndroidContact("Thomas Shfar","0884843770982", R.drawable.man3),
            new AndroidContact("Jessica Elan","484848737782", R.drawable.woman2),
            new AndroidContact("Tania Ian","0448480763702", R.drawable.woman3),
            new AndroidContact("Homer","9933799332", R.drawable.man4),
            new AndroidContact("Carlia Domy","838484800982", R.drawable.woman4),
            new AndroidContact("Kate Justine","5633799332", R.drawable.woman5),
            new AndroidContact("Alex Hunter","108484800982", R.drawable.man5)
    };*/
}