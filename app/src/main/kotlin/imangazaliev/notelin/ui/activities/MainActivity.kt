package imangazaliev.notelin.ui.activities

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.pawegio.kandroid.onQuerySubmit
import imangazaliev.notelin.Presenters
import imangazaliev.notelin.R
import imangazaliev.notelin.mvp.common.MvpAppCompatActivity
import imangazaliev.notelin.mvp.models.Note
import imangazaliev.notelin.mvp.presenters.MainPresenter
import imangazaliev.notelin.mvp.views.MainView
import imangazaliev.notelin.ui.adapters.NotesAdapter
import imangazaliev.notelin.ui.commons.ItemClickSupport
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : MvpAppCompatActivity(), MainView, ItemClickSupport.OnItemClickListener, ItemClickSupport.OnItemLongClickListener {

    @InjectPresenter
    lateinit var mPresenter: MainPresenter
    var mNoteContextDialog: MaterialDialog? = null
    var mNoteDeleteDialog: MaterialDialog? = null
    var mNoteInfoDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ItemClickSupport.addTo(rvNotesList).setOnItemClickListener(this)
        ItemClickSupport.addTo(rvNotesList).setOnItemLongClickListener (this)

        fabButton.attachToRecyclerView(rvNotesList)
        fabButton.setOnClickListener {
            val note = mPresenter.createNote()
            mPresenter.openNote(this, note)
        }
    }

    override fun onNotesLoaded(notes: ArrayList<Note>) {
        rvNotesList.adapter = NotesAdapter(notes)
        updateView()
    }

    override fun updateView() {
        rvNotesList.adapter.notifyDataSetChanged()
        if (rvNotesList.adapter.itemCount == 0) {
            rvNotesList.visibility = View.GONE
            tvNotesIsEmpty.visibility = View.VISIBLE
        } else {
            rvNotesList.visibility = View.VISIBLE
            tvNotesIsEmpty.visibility = View.GONE
        }
    }

    override fun onSearchResult(notes: ArrayList<Note>) {
        rvNotesList.adapter = NotesAdapter(notes)
    }

    override fun onNoteDeleted(note: Note) {
        (rvNotesList.adapter as NotesAdapter).deleteNote(note)
        updateView()
        Toast.makeText(this, R.string.note_is_deleted, Toast.LENGTH_SHORT).show()
    }

    override fun onAllNotesDeleted() {
        updateView()
        fabButton.show();
        Toast.makeText(this, R.string.all_notes_is_deleted, Toast.LENGTH_SHORT).show()
    }

    override fun showNoteContextDialog(notePosition: Int) {
        mNoteContextDialog = MaterialDialog.Builder(this)
                .items(R.array.main_note_context)
                .itemsCallback { dialog, view, position, text ->
                    onContextDialogItemClick(position, notePosition)
                    mPresenter.hideNoteContextDialog()
                }
                .cancelListener { mPresenter.hideNoteContextDialog() }
                .show();
    }

    override fun hideNoteContextDialog() {
        mNoteContextDialog?.dismiss()
    }

    override fun showNoteDeleteDialog(notePosition: Int) {
        mNoteDeleteDialog = MaterialDialog.Builder(this)
                .title("Удаление заметки")
                .content("Вы действительно хотите удалить заметку")
                .positiveText("Да")
                .negativeText("Нет")
                .onPositive { materialDialog, dialogAction ->
                    mPresenter.deleteNoteByPosition(notePosition)
                    mNoteInfoDialog?.dismiss()
                }
                .onNegative { materialDialog, dialogAction -> mPresenter.hideNoteDeleteDialog() }
                .cancelListener { mPresenter.hideNoteDeleteDialog() }
                .show()
    }

    override fun hideNoteDeleteDialog() {
        mNoteDeleteDialog?.dismiss()
    }

    override fun showNoteInfoDialog(noteInfo: String) {
        mNoteInfoDialog = MaterialDialog.Builder(this)
                .title("Информация о заметке")
                .positiveText("Ок")
                .content(noteInfo)
                .onPositive { materialDialog, dialogAction -> mPresenter.hideNoteInfoDialog() }
                .cancelListener { mPresenter.hideNoteInfoDialog() }
                .show()
    }

    override fun hideNoteInfoDialog() {
        mNoteInfoDialog?.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        var myActionMenuItem = menu.findItem(R.id.action_search);
        var searchView = myActionMenuItem.actionView as SearchView;
        searchView.onQuerySubmit { query -> mPresenter.search(query) }
        searchView.setOnCloseListener { mPresenter.search(""); false }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuDeleteAllNotes -> mPresenter.deleteAllNotes()
            R.id.menuSortByName -> mPresenter.sortNotesBy(MainPresenter.SortNotesBy.NAME)
            R.id.menuSortByDate -> mPresenter.sortNotesBy(MainPresenter.SortNotesBy.DATE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemLongClicked(recyclerView: RecyclerView?, notePosition: Int, v: View?): Boolean {
        mPresenter.showNoteContextDialog(notePosition)
        return true
    }

    fun onContextDialogItemClick(contextItemPosition: Int, notePosition: Int) {
        when (contextItemPosition) {
            0 -> mPresenter.openNote(this, notePosition)
            1 -> mPresenter.showNoteInfo(notePosition)
            2 -> mPresenter.showNoteDeleteDialog(notePosition)
        }
    }

    override fun onItemClicked(recyclerView: RecyclerView?, position: Int, v: View?) {
        mPresenter.openNote(this, position)
    }

}