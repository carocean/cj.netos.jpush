package cj.netos.jpush.terminal.plugin;

public class PersonUnreadMessageTotal {
    String person;
    long count;

    public PersonUnreadMessageTotal() {
    }

    public PersonUnreadMessageTotal(String person, long count) {
        this.person = person;
        this.count = count;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
