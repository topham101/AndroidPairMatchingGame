package mobile.labs.acw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matt on 26/03/2017.
 */

public class PuzzleDBHelper extends SQLiteOpenHelper {

    //region Members
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Puzzles.db";
    private Context m_Context;
    //endregion

    //region Override Functions & Constructor
    public PuzzleDBHelper(Context pContext) {
        super(pContext, DATABASE_NAME, null, DATABASE_VERSION);
        m_Context = pContext;
    }
    @Override
    public void onOpen(SQLiteDatabase pDb) {
        super.onOpen(pDb);
        if (!pDb.isReadOnly()) {
            pDb.setForeignKeyConstraintsEnabled(true);
        }
    }
    @Override
    public void onCreate(SQLiteDatabase pDb){
        pDb.execSQL(PuzzleDBContract.SQL_CREATE_PICTURESET_TABLE);
        pDb.execSQL(PuzzleDBContract.SQL_CREATE_PICTURE_TABLE);
        pDb.execSQL(PuzzleDBContract.SQL_CREATE_PUZZLE_TABLE);
        pDb.execSQL(PuzzleDBContract.SQL_CREATE_PICTURESET_RELATIONSHIP_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase pDb, int pOldVersion, int pNewVerison) {
        pDb.execSQL(PuzzleDBContract.SQL_DELETE_PICTURESET_RELATIONSHIP_TABLE);
        pDb.execSQL(PuzzleDBContract.SQL_DELETE_PUZZLE_TABLE);
        pDb.execSQL(PuzzleDBContract.SQL_DELETE_PICTURESET_TABLE);
        pDb.execSQL(PuzzleDBContract.SQL_DELETE_PICTURE_TABLE);

        onCreate(pDb);
    }
    //endregion

    //region add functions
    public int addNewPuzzle(String pPuzzleName)
    {
        // If Puzzle doesn't exist
        if(!PuzzleExists(pPuzzleName)) {
            // Download Puzzle Json
            PuzzleRecord downloadedPuzzle = DownloadPuzzleRecord(
                    "http://www.hull.ac.uk/php/349628/08027/acw/puzzles/" + "puzzle" + pPuzzleName.substring(7, pPuzzleName.length()) + ".json");
            // Puzzle insert Query
            return insertNewPuzzle(downloadedPuzzle);
        }
        return -1;
    }
    //endregion

    //region db EXISTS Functions
    private boolean PuzzleExists(String pPuzzleName)
    {
        if (!ParameterCheck(pPuzzleName))
        {
            return false;
        }
        String queryString = "SELECT _id FROM Puzzle WHERE Name = ?;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[] { pPuzzleName });
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return false;
        }
        else
        {
            cursor.close();
            return true;
        }
    }
    private boolean PictureSetExists(int pPictureSet_id)
    {
        String queryString = "SELECT Name FROM PictureSet WHERE _id = " + pPictureSet_id + ";";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return false;
        }
        else
        {
            cursor.close();
            return true;
        }
    }
    private boolean PictureSetExists(String pPictureSet_Name)
    {
        if (!ParameterCheck(pPictureSet_Name))
        {
            return false;
        }
        String queryString = "SELECT _id FROM PictureSet WHERE Name = ?;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[] { pPictureSet_Name });
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return false;
        }
        else
        {
            cursor.close();
            return true;
        }
    }
    private boolean PictureExists(String pPictureSet_Name)
    {
        if (!ParameterCheck(pPictureSet_Name))
        {
            return false;
        }
        String queryString = "SELECT _id FROM Picture WHERE Name = ?;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[] { pPictureSet_Name });
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return false;
        }
        else
        {
            cursor.close();
            return true;
        }
    }
    private boolean PictureSetRelationshipExists(int pPictureSet_ID, int pPicture_ID)
    {
        String queryString = "SELECT _id FROM PictureSet_Relationship WHERE PictureSet_ID = " + pPictureSet_ID + " AND Picture_ID = " + pPicture_ID + ";";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return false;
        }
        else
        {
            cursor.close();
            return true;
        }
    }
    //endregion

    //region db GET Functions
    public PuzzleRecord[] getAllDBPuzzles()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Puzzle;", null);
        PuzzleRecord[] returnPuzzleArray = new PuzzleRecord[cursor.getCount()];
        int i = 0;
        if (cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                // Get Each Column
                int ID = cursor.getInt(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry._ID));
                String Name = cursor.getString(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_NAME));
                int PictureSet_ID = cursor.getInt(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_PICTURESET_ID));
                int Rows = cursor.getInt(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_ROWS));
                String HighScore = cursor.getString(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_HIGHSCORE));

                String LayoutStr = cursor.getString(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_LAYOUT));
                String[] LayoutStrArray = LayoutStr.split("\\s*,\\s*");
                int[] Layout = new int[LayoutStrArray.length];
                for (int j = 0; j < LayoutStrArray.length; j++)
                {
                    Layout[j] = Integer.parseInt(LayoutStrArray[j]);
                }

                // Add record to return list
                PuzzleRecord tempPuzzle = new PuzzleRecord(ID, Name, PictureSet_ID, Rows, Layout, HighScore);
                returnPuzzleArray[i] = tempPuzzle;

                //Iterate
                cursor.moveToNext();
                i++;
            }
            cursor.close();
            return returnPuzzleArray;
        }
        else
        {
            cursor.close();
            return new PuzzleRecord[0];
        }
    }
    public PuzzleRecord getDBPuzzle(String pPuzzleName)
    {
        if (!ParameterCheck(pPuzzleName))
        {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Puzzle WHERE Name = ?;", new String[] { pPuzzleName });

        if (cursor.moveToFirst())
        {
            // Get Each Column
            int ID = cursor.getInt(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry._ID));
            String Name = cursor.getString(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_NAME));
            int PictureSet_ID = cursor.getInt(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_PICTURESET_ID));
            int Rows = cursor.getInt(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_ROWS));
            String HighScore = cursor.getString(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_HIGHSCORE));

            String LayoutStr = cursor.getString(cursor.getColumnIndex(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_LAYOUT));
            String[] LayoutStrArray = LayoutStr.split("\\s*,\\s*");
            int[] Layout = new int[LayoutStrArray.length];
            for (int j = 0; j < LayoutStrArray.length; j++)
            {
                Layout[j] = Integer.parseInt(LayoutStrArray[j]);
            }

            // return record
            PuzzleRecord newPuzzle = new PuzzleRecord(ID, Name, PictureSet_ID, Rows, Layout, HighScore);
            cursor.close();
            return newPuzzle;
        }
        else
        {
            cursor.close();
            return null;
        }
    }
    private int GetPictureSetID(String pPictureSet_Name)
    {
        if (!ParameterCheck(pPictureSet_Name))
        {
            return -1;
        }
        String queryString = "SELECT _id FROM PictureSet WHERE Name = ?;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[] { pPictureSet_Name });
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return -1;
        }
        else
        {
            cursor.moveToFirst();
            int _ID = cursor.getInt(cursor.getColumnIndexOrThrow(PuzzleDBContract.PictureSetEntry._ID));
            cursor.close();
            return _ID;
        }
    }
    private int GetPictureID(String pPicture_Name)
    {
        if (!ParameterCheck(pPicture_Name))
        {
            return -1;
        }
        String queryString = "SELECT _id FROM Picture WHERE Name = ?;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[] { pPicture_Name });
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return -1;
        }
        else
        {
            cursor.moveToFirst();
            int _ID = cursor.getInt(cursor.getColumnIndexOrThrow(PuzzleDBContract.PictureEntry._ID));
            cursor.close();
            return _ID;
        }
    }
    private String GetPictureName(int pPicture_ID)
    {
        String queryString = "SELECT Name FROM Picture WHERE _id = " + pPicture_ID + ";";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return null;
        }
        else
        {
            cursor.moveToFirst();
            String pictureName = cursor.getString(cursor.getColumnIndexOrThrow(PuzzleDBContract.PictureEntry.COLUMN_NAME_NAME));
            cursor.close();
            return pictureName;
        }
    }
    public String[] GetPictureSet(int pPictureSet_ID)
    {
        String queryString = "SELECT * FROM PictureSet_Relationship WHERE PictureSet_ID = " + pPictureSet_ID + ";";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.getCount() < 1)
        {
            cursor.close();
            return new String[0];
        }
        cursor.moveToFirst();
        String[] resultPictureSet = new String[cursor.getCount()];
        int i = 0;
        while (cursor.isAfterLast() == false)
        {
            int tempPicture_ID = cursor.getInt(cursor.getColumnIndexOrThrow(PuzzleDBContract.PictureSetRelationshipEntry.COLUMN_NAME_PICTURE_ID));
            String pictureName = GetPictureName(tempPicture_ID);
            if (pictureName == null)
            {
                cursor.close();
                return new String[0];
            }
            resultPictureSet[i] = pictureName;
            cursor.moveToNext();
            i++;
        }
        cursor.close();
        return resultPictureSet;
    }
    //endregion

    //region db INSERT Functions
    private int insertNewPuzzle(PuzzleRecord pNewPuzzle)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_NAME, pNewPuzzle.getName());
        values.put(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_PICTURESET_ID, pNewPuzzle.getPictureSet_ID());
        values.put(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_ROWS, pNewPuzzle.getRows());
        values.put(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_LAYOUT, pNewPuzzle.getLayoutString());

        long temp = db.insert(PuzzleDBContract.PuzzleEntry.TABLE_NAME, null, values);
        if (temp == -1)
            throw new IllegalArgumentException("INSERT SQL FAILURE");
        return (int)temp;
    }
    private int insertNewPictureSet(String pPictureSet_Name)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PuzzleDBContract.PictureSetEntry.COLUMN_NAME_NAME, pPictureSet_Name);

        long temp = db.insert(PuzzleDBContract.PictureSetEntry.TABLE_NAME, null, values);
        if (temp == -1)
            throw new IllegalArgumentException("INSERT SQL FAILURE");
        return (int)temp;
    }
    private int insertNewPictureSet_RelationShip(int pPictureSet_ID, int pPicture_ID)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PuzzleDBContract.PictureSetRelationshipEntry.COLUMN_NAME_PICTURESET_ID, pPictureSet_ID);
        values.put(PuzzleDBContract.PictureSetRelationshipEntry.COLUMN_NAME_PICTURE_ID, pPicture_ID);

        long temp = db.insert(PuzzleDBContract.PictureSetRelationshipEntry.TABLE_NAME, null, values);
        if (temp == -1)
            throw new IllegalArgumentException("INSERT SQL FAILURE");
        return (int)temp;
    }
    private int insertNewPicture(String pPicture_Name)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PuzzleDBContract.PictureEntry.COLUMN_NAME_NAME, pPicture_Name);

        long temp = db.insert(PuzzleDBContract.PictureEntry.TABLE_NAME, null, values);
        if (temp == -1)
            throw new IllegalArgumentException("INSERT SQL FAILURE");
        return (int)temp;
    }
    //endregion

    //region db UPDATE Functions
    public int UpdateHighScore(int pPuzzle_ID, int pHighscore)
    {
        SQLiteDatabase db = getWritableDatabase();
        String filter = PuzzleDBContract.PuzzleEntry._ID  + " = " + pPuzzle_ID;
        ContentValues values = new ContentValues();
        values.put(PuzzleDBContract.PuzzleEntry.COLUMN_NAME_HIGHSCORE, pHighscore);
        return db.update(PuzzleDBContract.PuzzleEntry.TABLE_NAME, values, filter, null);

        //Cursor cursor = db.rawQuery("UPDATE " + PuzzleDBContract.PuzzleEntry.TABLE_NAME + " SET "
        //                + PuzzleDBContract.PuzzleEntry.COLUMN_NAME_HIGHSCORE + " = " + pHighscore
        //                + ", WHERE " + PuzzleDBContract.PuzzleEntry._ID + " = " + pPuzzle_ID + ";"
        //                , null);
        //return cursor.getCount();
    }
    //endregion

    //region downloadJSON Functions
    private PuzzleRecord DownloadPuzzleRecord(String puzzleUrl)
    {
        PuzzleRecord result = null;
        try
        {
            String streamResult = "";
            InputStream stream = (InputStream) new URL(puzzleUrl).getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = "";
            while(line != null)
            {
                streamResult += line;
                line = reader.readLine();
            }

            JSONObject json = new JSONObject(streamResult);
            JSONObject puzzleObj = json.getJSONObject("Puzzle");

            String Name = puzzleObj.getString("Id");
            String pictureSetName = puzzleObj.getString("PictureSet");
            int Rows = puzzleObj.getInt("Rows");

            JSONArray layoutObj = puzzleObj.getJSONArray("Layout");
            int[] Layout = new int[layoutObj.length()];
            for (int i = 0; i < layoutObj.length(); i++)
            {
                Layout[i] = layoutObj.getInt(i);
            }

            // If Picture Set does not exist, download it and insert it into the DB THEN return the _ID
            int PictureSet_ID = DownloadPictureSetIfDoesntExist(pictureSetName);

            result = new PuzzleRecord(Name, PictureSet_ID, Rows, Layout);
        }
        catch (Exception e) {}
        return result;
    }
    private int DownloadPictureSetIfDoesntExist(String pPictureSetName)
    {
        // IF IT DOES EXIST, CHECK PICTURES ANYWAY ------- &&&&& MAKE ALL THIS NO-INTERNET SAFE!

        String pictureSetUrl = "http://www.hull.ac.uk/php/349628/08027/acw/picturesets/" + pPictureSetName;

        try
        {
            int pictureSet_ID;
            String[] pictureSet;

            pictureSet_ID = GetPictureSetID(pPictureSetName);
            pictureSet = GetPictureSet(pictureSet_ID);

            if(!PictureSetExists(pPictureSetName) || pictureSet_ID <= 0 || pictureSet.length == 0) // CHECK THIS WORKS
            {
                if(!PictureSetExists(pPictureSetName)) {
                    pictureSet_ID = insertNewPictureSet(pPictureSetName);
                }

                String streamResult = "";
                InputStream stream = (InputStream) new URL(pictureSetUrl).getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                while(line != null)
                {
                    streamResult += line;
                    line = reader.readLine();
                }

                JSONObject json = new JSONObject(streamResult);
                JSONArray pictureSetObj = json.getJSONArray("PictureFiles");

                pictureSet = new String[pictureSetObj.length()];
                for (int i = 0; i < pictureSetObj.length(); i++)
                {
                    String pictureName = pictureSetObj.getString(i);
                    pictureSet[i] = pictureName;
                    int picture_ID = DownloadPictureIfDoesntExist(pictureName);

                    if (!PictureSetRelationshipExists(pictureSet_ID, picture_ID))
                        insertNewPictureSet_RelationShip(pictureSet_ID, picture_ID);
                }
            }
            else
            {
                for (int i = 0; i < pictureSet.length; i++)
                {
                    int picture_ID = DownloadPictureIfDoesntExist(pictureSet[i]);

                    if (!PictureSetRelationshipExists(pictureSet_ID, picture_ID))
                        insertNewPictureSet_RelationShip(pictureSet_ID, picture_ID);
                }
            }

            return pictureSet_ID;
        }
        catch (Exception e) {}
        return -1;
    }
    private int DownloadPictureIfDoesntExist(String pPictureName)
    {
        File pictureFile = new File(pPictureName);

        boolean PictureRecordExists = PictureExists(pPictureName);
        boolean PictureFileExists = pictureFile.exists();

        if (!PictureFileExists)
        {
            // Download Image
            try
            {
                String pictureUrl = "http://www.hull.ac.uk/php/349628/08027/acw/images/" + pPictureName;
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(pictureUrl).getContent());
                FileOutputStream writer = null;
                try 
                {
                    writer = m_Context.getApplicationContext().openFileOutput(pPictureName, m_Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, writer);
                }
                catch (Exception e) { Log.i("My Exception", e.getMessage()); }
                finally { writer.close(); }
            }
            catch (Exception e) { Log.i("My Exception", e.getMessage()); }
        }
        if (!PictureRecordExists)
        {
            // Insert Image into DB
            return insertNewPicture(pPictureName);
        }
        else return GetPictureID(pPictureName);
    }
    //endregion

    //region Static Functions
    private static boolean ParameterCheck(String input)
    {
        if (input.contains("',;=") || input.isEmpty() || input == null)
        {
            return false;
        }
        else return true;
    }
    //endregion
}
