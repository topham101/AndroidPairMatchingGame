package mobile.labs.acw;

/**
 * Created by Matt on 26/03/2017.
 */

public class PuzzleRecord {

    //region Members
    private String m_Name, m_HighScore;
    private int[] m_Layout;
    private int m_Rows, m_ID, m_PictureSet_ID;
    //endregion

    //region Constructors
    public PuzzleRecord(String _name, int _pictureSet, int _rows, int[] _layout)
    {
        m_ID = -1;
        m_Name = _name;
        m_PictureSet_ID = _pictureSet;
        m_Rows = _rows;
        m_Layout = _layout;
        m_HighScore = "";
    }

    public PuzzleRecord(int _iD, String _name, int _pictureSet, int _rows, int[] _layout, String _highScore)
    {
        m_ID = _iD;
        m_Name = _name;
        m_PictureSet_ID = _pictureSet;
        m_Rows = _rows;
        m_Layout = _layout;
        m_HighScore = _highScore;
    }
    //endregion

    //region GET Methods
    public int getPictureSet_ID()
    {
        return m_PictureSet_ID;
    }
    public String getHighScore()
    {
        return m_HighScore;
    }
    public int[] getLayout()
    {
        return m_Layout;
    }
    public String getName()
    {
        return m_Name;
    }
    public int getRows()
    {
        return m_Rows;
    }
    public int getID()
    {
        return m_ID;
    }
    public String getLayoutString()
    {
        String result = "";
        for (int i = 0; i < m_Layout.length; i++)
        {
            result += m_Layout[i];
            if (i != m_Layout.length - 1)
            {
                result += ",";
            }
        }
        return result;
    }
    //endregion
}
