package am.ik.categolj.daccess.mirage.entities;

public class EntryCategory {
    public Long entryId;
    public Long categoryId;

    public EntryCategory() {
    }

    public EntryCategory(Long entryId, Long categoryId) {
        super();
        this.entryId = entryId;
        this.categoryId = categoryId;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
