<!--

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

-->

<!--

evaluation.xsl

Formats the evaluated submissions XML file into a table for use in Excel.

For more information contact: Brendan Wyse <bjwyse@gmail.com>

-->

<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Global to change generated table -->
<!-- Set to 'no' to see raw scores only -->
<xsl:variable name="scores_only">no</xsl:variable>
	
<xsl:template match="/">
  <html>
  <body>
  <xsl:if test="$scores_only = 'no'">
	<h2>Evaluation Results</h2>
  
	<h3>Instances: <xsl:value-of select="count(dataset/instance)"/></h3>
	<h3>Possible Questions: <xsl:value-of select="count(dataset/instance/targetQuestionType) * 2"/></h3>
  </xsl:if>
  
  <table border="1">
	
	<xsl:if test="$scores_only = 'yes'">
		<!-- Display each submission -->
		<tr bgcolor="whitesmoke">
		<td colspan="2"></td>
		<td>Relevance</td>
		<td>Question Type</td>
		<td>Correctness</td>
		<td>Ambiguity</td>
		<td>Variety</td>
		</tr>
	</xsl:if>
	
    <xsl:for-each select="dataset/instance">
	
		<xsl:if test="$scores_only = 'no'">
			<tr bgcolor="#9acd32">
			<th colspan="6">Instance: <xsl:value-of select="@id" /><xsl:text> -- </xsl:text><i><xsl:value-of select="id"/><xsl:text> </xsl:text><xsl:value-of select="source"/></i></th>
			</tr>

			<!-- Display the input sentence -->
			<tr bgcolor="whitesmoke">
			<td colspan="1"><center><b>Sentence</b></center></td>
			<td colspan="5"><center><b>Scoring</b></center></td></tr>
			<!-- Display each submission -->
			<tr bgcolor="whitesmoke">
			<td colspan="1"><xsl:value-of select="text"/>
			
			<!-- Show the target question types -->
			<xsl:text> [TARGETS {</xsl:text>
			<xsl:for-each select="targetQuestionType">
				<xsl:value-of select="."/>
				<xsl:if test="position()!=last()">
					<xsl:text>, </xsl:text>
				</xsl:if>
			</xsl:for-each>
			<xsl:text>}]</xsl:text> 
			
			</td>
			<td>Relevance</td>
			<td>Question Type</td>
			<td>Correctness</td>
			<td>Ambiguity</td>
			<td>Variety</td>
			</tr>
		</xsl:if>
	
		<!-- Use to select only a particular submission -->
		<!-- <xsl:for-each select="submission[@id='b']"> -->
		<xsl:for-each select="submission">
			<!-- Display submission header row -->
			<xsl:if test="$scores_only = 'no'">
				<tr>
				<td colspan="6"><b>Submission <xsl:value-of select="@id"/></b></td>
				</tr>
			</xsl:if>
		
			<xsl:for-each select="question">
				<tr>
				<xsl:if test="$scores_only = 'yes'">
					<td><xsl:value-of select="../@id"/></td>
				</xsl:if>
	
				<!-- Show the question -->
				<td><xsl:value-of select="." />

				<!-- Show the initials of the raters in parentheses -->
				<xsl:text>(</xsl:text>
				<xsl:for-each select="rating">
					<xsl:value-of select="@rater"/>
					<xsl:if test="position()!=last()">
						<xsl:text>, </xsl:text>
					</xsl:if>
				</xsl:for-each>
				<xsl:text>)</xsl:text> 
	
				<!-- Display an asterix after the question if it was only rated once -->
				<xsl:if test="count(rating) &lt; 2">
					<xsl:text>*</xsl:text>
				</xsl:if>
				</td>
				<td>
				<!-- Show the average score -->
				<b><xsl:value-of select="format-number(sum(rating/@relevance) div count(rating/@relevance), '###.0')"/></b>
				<!-- Show the individual raters scores -->
				<xsl:if test="$scores_only = 'no'">
					<xsl:text>....(</xsl:text>
					<xsl:for-each select="rating">
						<xsl:value-of select="@relevance"/>
						<xsl:if test="position()!=last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>)</xsl:text>
				</xsl:if>
				</td>
				<td>
				<!-- Show the average score -->
				<b><xsl:value-of select="format-number(sum(rating/@questionType) div count(rating/@questionType), '###.0')"/></b>
				<!-- Show the individual raters scores -->
				<xsl:if test="$scores_only = 'no'">
					<xsl:text>....(</xsl:text>
					<xsl:for-each select="rating">
						<xsl:value-of select="@questionType"/>
						<xsl:if test="position()!=last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>)</xsl:text>
				</xsl:if>
				</td>
				<td>
				<!-- Show the average score -->
				<b><xsl:value-of select="format-number(sum(rating/@correctness) div count(rating/@correctness), '###.0')"/></b>
				<!-- Show the individual raters scores -->
				<xsl:if test="$scores_only = 'no'">
					<xsl:text>....(</xsl:text>
					<xsl:for-each select="rating">
						<xsl:value-of select="@correctness"/>
						<xsl:if test="position()!=last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>	
					<xsl:text>)</xsl:text>
				</xsl:if>
				</td>
				<td>
				<!-- Show the average score -->
				<b><xsl:value-of select="format-number(sum(rating/@ambiguity) div count(rating/@ambiguity), '###.0')"/></b>
				<!-- Show the individual raters scores -->
				<xsl:if test="$scores_only = 'no'">
					<xsl:text>....(</xsl:text>
					<xsl:for-each select="rating">
						<xsl:value-of select="@ambiguity"/>
						<xsl:if test="position()!=last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>)</xsl:text>
				</xsl:if>
				</td>
				<td>
				<!-- Show the average score -->
				<b><xsl:value-of select="format-number(sum(rating/@variety) div count(rating/@variety), '###.0')"/></b>
				<!-- Show the individual raters scores -->
				<xsl:if test="$scores_only = 'no'">
					<xsl:text>....(</xsl:text>
					<xsl:for-each select="rating">
						<xsl:value-of select="@variety"/>
						<xsl:if test="position()!=last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>)</xsl:text>
				</xsl:if>
				</td>
				</tr>
			</xsl:for-each>
		</xsl:for-each>
    </xsl:for-each>
  </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>