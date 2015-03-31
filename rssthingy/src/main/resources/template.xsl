<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:template match="/rss">
      <html>
         <head>
            <title>RSS Thingy</title>
         </head>
         <body>
            <h1>RSS Thingy</h1>
            <table>
               <thead />
               <tbody>
                  <xsl:for-each select="channel/item">
                     <tr>
                        <td>
                           <a href="">
                              <xsl:value-of select="title" />
                           </a>
                        </td>
                     </tr>
                  </xsl:for-each>
               </tbody>
            </table>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>