# LCBO
Android, Retrofit, ORMLite, Picasso, ... lcboapi.com ...<br><br><br>
Well, most of tasks implemented.<br>
Not implemented:<br>
Product search screen and Favorite stores.<br>
Product search screen algorithmically similar to stores search. So no problems to make it.<br>
For favourite stores crated table in DB, so rest implementation is work with GUI design and little code.<br>
<br>
The rest work fine.





# Application specifications:<br>
<b>Android test application specifications</b><br>
Target : implement android app for displaying data from remote server,  and store to local  database in linked SQL tables.<br><br>
<b><h2>Global specs:</h2></b>
App must work without internet (with local db), every downloaded data should be saved.<br>
Data should be downloaded to local storage and displayed to user from db.<br>
Database should be cleaned once a day.<br>
Store or Product items data should be downloaded in parts and updated when the user reaches the end of the downloaded list until he reaches the end of remote servers list.<br>
App design must follow Google’s Material Design guidelines.<br>
Optionally use ContentProvider to work with database.<br><br>
<b><h3>API</h3></b>
To start working with api you must register and generate api access token.<br>
Main reference : https://lcboapi.com/<br>
Documentation : https://lcboapi.com/docs/v1<br><br>
<b><h3>App structure</h3></b>
App will use navigation drawer  to navigate between screens, it must contain the following items:<br>
• Stores (main screen)<br>
• Optionally: Favorites stores<br>
• Products by categories<br>
• Products search with options<br>
• Shopping cart<br>
• About<br>
<br>
<h3>Stores screen</h3>
Must contain :<br>
• Stores list<br>
• Add search function to toolbar, with search options button which opens a dialog with search options (“has_wheelchair_accessability”, “has_bilingual_services”, “has_parking” fields etc.)<br>
• After clicking on store item screen with store details should be displayed.<br>
Optionally: set up stores count downloaded at time in settings.<br><br>
<h3>Store detail screen</h3>
This screen gives a detailed overview on the selected store from the store model. The presentation of the data is up to you.<br>
On the store details screen should be ability to :<br>
• Open list of store’s products (at this point connections should be created in db tables) p.s. use /products?store_id=<storeId> - endpoint to get data<br>
• Open the map with marker on its location<br>
• Make a call on store’s phone number (if the store has more than one phone number then give user possibility to choose one)<br>
Optionally possibility to add selected store to favorites.<br><br>

<h3>Products by categories</h3>
This screen contains TabLayout with ViewPager of products sorted by categories “Beer”, “Wine”, “Spirits” each one containing a  list of products with pictures and add to cart button.<br>
Click on product should open a dialog with product’s detailed info.<br>
A click on the cart button should open a dialog to select product count and add selected product to user shopping cart.<br><br>

<h3>Product search screen</h3>
List of products with search field in toolbar and options menu  similar to the store screen (option fields could be “is_seasonal”, “is_kosher”, “has_limited_time_offer” fields etc.)<br>
A click on product should open a dialog with product’s detailed info.<br>
<br>
<h3>Shopping cart screen</h3>
Displays the list of product selected by user and items count for each product with possibility to edit or delete this product from cart.<br>
<h3>About</h3>
Simple screen with data about the app purposes<br>
<br><br><br>


<br><br><br>
<b>LCBO_1.00_Build_1.apk _2017.03.07 06:02</b>:<br>
CRC32: 96BF4679<br>
MD5: 8A1918687AAF7AC8A6F237A6EEE535B8<br>
SHA-1: E25D99C709B8C3282826CF96433FAAC48CE5C27D<br>
<br>
Download links:<br>
<a href="https://drive.google.com/open?id=0BzoKZrHsxcSbNmlGMzRidXNUc3M" target="_blank">Google Drive</a><br>
<a href="https://yadi.sk/d/QnqR9mPZ3FBDYS" target="_blank">Yandex Disk</a><br>
