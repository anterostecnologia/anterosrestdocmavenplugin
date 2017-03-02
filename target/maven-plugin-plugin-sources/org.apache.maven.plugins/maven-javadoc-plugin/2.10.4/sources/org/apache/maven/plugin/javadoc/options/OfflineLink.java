// =================== DO NOT EDIT THIS FILE ====================
// Generated by Modello 1.8.1,
// any modifications will be overwritten.
// ==============================================================

package org.apache.maven.plugin.javadoc.options;

/**
 * An offline link parameter.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings( "all" )
public class OfflineLink
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The url of the link.
     */
    private String url;

    /**
     * The location of the link.
     */
    private String location;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method equals.
     * 
     * @param other
     * @return boolean
     */
    public boolean equals( Object other )
    {
        if ( this == other )
        {
            return true;
        }

        if ( !( other instanceof OfflineLink ) )
        {
            return false;
        }

        OfflineLink that = (OfflineLink) other;
        boolean result = true;

        result = result && ( getUrl() == null ? that.getUrl() == null : getUrl().equals( that.getUrl() ) );
        result = result && ( getLocation() == null ? that.getLocation() == null : getLocation().equals( that.getLocation() ) );

        return result;
    } //-- boolean equals( Object )

    /**
     * Get the location of the link.
     * 
     * @return String
     */
    public String getLocation()
    {
        return this.location;
    } //-- String getLocation()

    /**
     * Get the url of the link.
     * 
     * @return String
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl()

    /**
     * Method hashCode.
     * 
     * @return int
     */
    public int hashCode()
    {
        int result = 17;

        result = 37 * result + ( url != null ? url.hashCode() : 0 );
        result = 37 * result + ( location != null ? location.hashCode() : 0 );

        return result;
    } //-- int hashCode()

    /**
     * Set the location of the link.
     * 
     * @param location
     */
    public void setLocation( String location )
    {
        this.location = location;
    } //-- void setLocation( String )

    /**
     * Set the url of the link.
     * 
     * @param url
     */
    public void setUrl( String url )
    {
        this.url = url;
    } //-- void setUrl( String )

    /**
     * Method toString.
     * 
     * @return String
     */
    public java.lang.String toString()
    {
        StringBuilder buf = new StringBuilder( 128 );

        buf.append( "url = '" );
        buf.append( getUrl() );
        buf.append( "'" );
        buf.append( "\n" ); 
        buf.append( "location = '" );
        buf.append( getLocation() );
        buf.append( "'" );

        return buf.toString();
    } //-- java.lang.String toString()

}
