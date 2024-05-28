package com.jrj.mobilecolorpicker

/*import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar*/
import android.graphics.Bitmap
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.set
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.jrj.mobilecolorpicker.ui.theme.MobileColorPickerTheme
import kotlin.math.asin
import kotlin.math.max
import kotlin.math.min

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
            Spacer(modifier.size(30.dp))
            Box {
                ImageDisplayZone(modifier = Modifier, galleryLauncher = galleryLauncher,imageUri=chosenImageUri)
                //PickerBox()
            }
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            IconButton(onClick = {
                galleryLauncher?.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))

            }) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Open gallery",
                    tint = Color.White
                )
            }},
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageDisplayZone(modifier: Modifier = Modifier, galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>?, imageUri: Uri?) {
    var colorPicked by remember { mutableStateOf(0) }
    var imgBitmap by remember { mutableStateOf(Bitmap.createBitmap(1,1,Bitmap.Config.RGBA_F16)) }

    var scale by remember { mutableStateOf(1f) } //Reset those values when picking an image
    var translationOffset by remember { mutableStateOf(Offset.Zero) }
    var imgSize by remember { mutableStateOf(IntSize.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        translationOffset += offsetChange
    }

    fun resetScale(){
        scale=1f
        translationOffset=Offset.Zero
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        if (imageUri != null){

            Box (
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(20.dp))
                    .size(width = 350.dp, height = 600.dp)
                    .fillMaxSize()
                    .background(Color.LightGray.copy(0.5f))
                    //.indication(interactionSource, LocalIndication.current)
                    .combinedClickable(
                        //Just here to have the ripple might change later
                        onClick = { },
                        onDoubleClick = {
                            if (scale != 1f || translationOffset != Offset.Zero) {
                                //println(translationOffset)
                                resetScale()
                            }
                        },
                    )
                    .padding(10.dp)
                )
            {
                AsyncImage(
                    onState = { state ->
                        val imgState = state
                        if (imgState is AsyncImagePainter.State.Success){ imgBitmap = imgState.result.drawable.toBitmap().copy(Bitmap.Config.RGBA_F16, true)}
                    },
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            imgSize = coordinates.size
                        }

                        .graphicsLayer(
                            scaleX = if (scale > 1) scale else max(0.5f, scale),
                            scaleY = if (scale > 1) scale else max(0.5f, scale),
                            translationX = if (translationOffset.x > 0) min(
                                800f * scale,
                                translationOffset.x
                            ) else max(-800f * scale, translationOffset.x),
                            translationY = if (translationOffset.y > 0) min(
                                800f * scale,
                                translationOffset.y
                            ) else max(-800f * scale, translationOffset.y)
                        )
                        .transformable(state = state)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { tapOffset: Offset ->
                                    if (scale == 1f && translationOffset == Offset.Zero) {
                                        /*TO DO: zoom in where the tap happened more acurately */
                                        var moveItX =
                                            (-(tapOffset.x - (size.width / 2f)) * 2f).coerceIn(
                                                -size.width / 2f,
                                                size.width / 2f
                                            )
                                        var moveItY =
                                            (-(tapOffset.y - (size.height / 2f)) * 2f).coerceIn(
                                                -size.height / 2f,
                                                size.height / 2f
                                            )

                                        translationOffset = Offset(moveItX, moveItY)
                                        scale = 2f

                                    } else {
                                        resetScale()
                                    }
                                },
                                onTap = { tapOffset: Offset ->
                                    var bitmapOffset = tapOffset.copy()
                                    colorPicked = getColorOnPixel(bitmapOffset,imgBitmap)
                                },
                                ) },
                    model = imageUri,
                    contentDescription = "Image chosen in the gallery",
                )
                PickerBox(colorPicked,imgBitmap)
            }
        }
        else{
            Box (
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .clip(shape = RoundedCornerShape(20.dp))
                    .size(width = 350.dp, height = 600.dp)
                    .background(Color.LightGray)
                    .clickable {
                        galleryLauncher?.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }
                )
            {
                Icon(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Open gallery",
                    tint = Color.White)
            }
        }
    }
}

@Composable
fun PickerBox(color: Int,imgBitmap: Bitmap){
    //Image(bitmap = imgBitmap.asImageBitmap(), contentDescription = "")
    Box (
        modifier = Modifier
            .size(100.dp)
            .background(
                color = Color(color)
            )
    )
}

fun getColorOnPixel(bitmapOffset: Offset,imageBitmap: Bitmap): Int {
    var pixel = imageBitmap.getPixel(bitmapOffset.x.toInt(),bitmapOffset.y.toInt())
    var redValue = android.graphics.Color.red(pixel)
    var blueValue = android.graphics.Color.blue(pixel)
    var greenValue = android.graphics.Color.green(pixel)
    var alphaValue = android.graphics.Color.alpha(pixel)

    var colorOnPixel = android.graphics.Color.argb(alphaValue,redValue,greenValue,blueValue)

    return colorOnPixel
}



