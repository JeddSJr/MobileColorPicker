package com.jrj.mobilecolorpicker

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
/*import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar*/
import coil.compose.AsyncImage
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jrj.mobilecolorpicker.ui.theme.MobileColorPickerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileColorPickerTheme {
                ColorPickerApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerApp(modifier: Modifier = Modifier){
    var chosenImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var galleryLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null){
            Log.d("PhotoPicker", "Selected URI: $uri")
            chosenImageUri = uri
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column {
            TopBar(galleryLauncher = galleryLauncher)
            Spacer(modifier.size(50.dp))
            ImageDisplayZone(modifier = Modifier, galleryLauncher = galleryLauncher,imageUri=chosenImageUri)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier,galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>?){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Color Picker",
                color = Color.White
            )
        },
        modifier = Modifier.background(Brush.verticalGradient(0f to Color.Black,0.3f to Color.Gray, 1f to Color.Transparent)),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            IconButton(onClick = {
                galleryLauncher?.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Open gallery",
                    tint = Color.White
                )
            } },
    )
}



@Composable
fun ImageDisplayZone(modifier: Modifier = Modifier, galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>?, imageUri: Uri?) {
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (imageUri != null){
            Box (modifier = modifier){
                AsyncImage(model = imageUri, contentDescription = "Image chosen in the gallery" )
            }
        }
        else{
            Box (
                modifier = modifier
                    .clip(shape = RoundedCornerShape(30.dp))
                    .size(width = 300.dp, height = 600.dp)
                    .background(Color.LightGray)
                    .clickable { galleryLauncher?.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center,

            ){
                Icon(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Open gallery",
                    tint = Color.White,

                )
            }
        }
    }
}



