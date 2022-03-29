package io.azcn;

public class Link {

    String href;
    String rel;
    String type;

    public String toHTML() {
        String res = "<link";

        if (href != null) {
            res += String.format(" href=%1$s", href);
        }

        if (rel != null) {
            res += String.format(" rel=%1$s", rel);
        }

        if (type != null) {
            res += String.format(" type=%1$s", type);
        }

        return res + ">";
    }

}
