package entities;

public class AndroidContactToContactAdapter extends Contact {

    public AndroidContactToContactAdapter(AndroidContact contact) {
        super(contact.getName(), contact.getNumber());
    }
}
