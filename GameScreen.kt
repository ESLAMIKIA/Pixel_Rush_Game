package com.example.thelastlantern

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val Road = Color(0xFF292F38)
private val Grass = Color(0xFF143D2D)
private val Yellow = Color(0xFFFFD447)
private val Mint = Color(0xFF27D6AE)

private enum class VehicleStyle {
    COMPACT, SEDAN, SPORT, SUV, VAN, TRUCK, MOTORCYCLE
}

private enum class VehicleModel(
    val style: VehicleStyle,
    val variant: Int,
    val width: Float,
    val height: Float
) {
    COMPACT_01(VehicleStyle.COMPACT, 0, 53f, 79f),
    COMPACT_02(VehicleStyle.COMPACT, 1, 55f, 82f),
    COMPACT_03(VehicleStyle.COMPACT, 2, 54f, 77f),
    COMPACT_04(VehicleStyle.COMPACT, 3, 57f, 84f),
    COMPACT_05(VehicleStyle.COMPACT, 4, 52f, 80f),
    COMPACT_06(VehicleStyle.COMPACT, 5, 56f, 81f),
    COMPACT_07(VehicleStyle.COMPACT, 6, 54f, 85f),
    COMPACT_08(VehicleStyle.COMPACT, 7, 57f, 78f),

    SEDAN_01(VehicleStyle.SEDAN, 0, 57f, 91f),
    SEDAN_02(VehicleStyle.SEDAN, 1, 59f, 94f),
    SEDAN_03(VehicleStyle.SEDAN, 2, 56f, 89f),
    SEDAN_04(VehicleStyle.SEDAN, 3, 60f, 96f),
    SEDAN_05(VehicleStyle.SEDAN, 4, 58f, 92f),
    SEDAN_06(VehicleStyle.SEDAN, 5, 61f, 94f),
    SEDAN_07(VehicleStyle.SEDAN, 6, 57f, 88f),
    SEDAN_08(VehicleStyle.SEDAN, 7, 60f, 91f),

    SPORT_01(VehicleStyle.SPORT, 0, 59f, 87f),
    SPORT_02(VehicleStyle.SPORT, 1, 62f, 85f),
    SPORT_03(VehicleStyle.SPORT, 2, 58f, 82f),
    SPORT_04(VehicleStyle.SPORT, 3, 63f, 89f),
    SPORT_05(VehicleStyle.SPORT, 4, 60f, 86f),
    SPORT_06(VehicleStyle.SPORT, 5, 61f, 91f),
    SPORT_07(VehicleStyle.SPORT, 6, 58f, 84f),
    SPORT_08(VehicleStyle.SPORT, 7, 64f, 88f),
    SPORT_09(VehicleStyle.SPORT, 8, 60f, 83f),
    SPORT_10(VehicleStyle.SPORT, 9, 62f, 90f),

    SUV_01(VehicleStyle.SUV, 0, 64f, 96f),
    SUV_02(VehicleStyle.SUV, 1, 66f, 99f),
    SUV_03(VehicleStyle.SUV, 2, 63f, 93f),
    SUV_04(VehicleStyle.SUV, 3, 68f, 101f),
    SUV_05(VehicleStyle.SUV, 4, 65f, 97f),
    SUV_06(VehicleStyle.SUV, 5, 67f, 95f),
    SUV_07(VehicleStyle.SUV, 6, 64f, 100f),
    SUV_08(VehicleStyle.SUV, 7, 69f, 98f),

    VAN_01(VehicleStyle.VAN, 0, 67f, 104f),
    VAN_02(VehicleStyle.VAN, 1, 70f, 108f),
    VAN_03(VehicleStyle.VAN, 2, 65f, 100f),
    VAN_04(VehicleStyle.VAN, 3, 69f, 106f),
    VAN_05(VehicleStyle.VAN, 4, 66f, 103f),
    VAN_06(VehicleStyle.VAN, 5, 71f, 110f),

    TRUCK_01(VehicleStyle.TRUCK, 0, 72f, 112f),
    TRUCK_02(VehicleStyle.TRUCK, 1, 74f, 116f),
    TRUCK_03(VehicleStyle.TRUCK, 2, 70f, 108f),
    TRUCK_04(VehicleStyle.TRUCK, 3, 75f, 114f),

    MOTOR_01(VehicleStyle.MOTORCYCLE, 0, 30f, 61f),
    MOTOR_02(VehicleStyle.MOTORCYCLE, 1, 32f, 64f),
    MOTOR_03(VehicleStyle.MOTORCYCLE, 2, 29f, 58f),
    MOTOR_04(VehicleStyle.MOTORCYCLE, 3, 33f, 63f),
    MOTOR_05(VehicleStyle.MOTORCYCLE, 4, 31f, 60f),
    MOTOR_06(VehicleStyle.MOTORCYCLE, 5, 34f, 65f),
    MOTOR_07(VehicleStyle.MOTORCYCLE, 6, 30f, 59f),
    MOTOR_08(VehicleStyle.MOTORCYCLE, 7, 32f, 62f)
}

private data class EnemyCar(
    val lane: Int,
    var y: Float,
    val color: Color,
    val model: VehicleModel
)

private fun vehicleSize(model: VehicleModel): Pair<Float, Float> {
    return model.width to model.height
}

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("pixel_traffic_progress", android.content.Context.MODE_PRIVATE)
    }

    var playerLane by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(preferences.getInt("best_score", 0)) }

    var gameOver by remember { mutableStateOf(false) }
    var crashFinished by remember { mutableStateOf(false) }
    var crashProgress by remember { mutableFloatStateOf(0f) }
    var hasStarted by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(true) }
    var showGarage by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var selectedCarIndex by remember { mutableIntStateOf(preferences.getInt("selected_car", 0)) }

    var roadOffset by remember { mutableFloatStateOf(0f) }
    var spawnTimer by remember { mutableIntStateOf(0) }
    var gameSpeed by remember { mutableFloatStateOf(7f) }

    val enemies = remember { mutableStateListOf<EnemyCar>() }

    val colors = listOf(
        Color(0xFFE53935),
        Color(0xFF1976D2),
        Color(0xFFFFB300),
        Color(0xFF7E57C2),
        Color(0xFFECEFF1),
        Color(0xFF37474F),
        Color(0xFF00897B),
        Color(0xFFEC407A),
        Color(0xFFCFB53B),
        Color(0xFF263238)
    )

    // Every available vehicle model can be collected through score milestones.
    val garageCars = VehicleModel.values().toList()
    val unlockStep = 15
    val unlockedCarCount = (1 + bestScore / unlockStep).coerceIn(1, garageCars.size)
    val activeCarIndex = selectedCarIndex.coerceIn(0, unlockedCarCount - 1)
    val playerModel = garageCars[activeCarIndex]
    val playerPaints = listOf(
        Color(0xFFFFD21F), Color(0xFF16D9C5), Color(0xFFFF4D6D),
        Color(0xFF4C8DFF), Color(0xFFB47CFF), Color(0xFFF4F6FA),
        Color(0xFFFF8B36), Color(0xFF79D65A), Color(0xFF222B3A)
    )
    val playerPaint = playerPaints[activeCarIndex % playerPaints.size]

    LaunchedEffect(Unit) {
        delay(1800)
        showSplash = false
    }

    fun restart() {
        playerLane = 1
        score = 0
        gameOver = false
        crashFinished = false
        crashProgress = 0f
        roadOffset = 0f
        spawnTimer = 0
        gameSpeed = 7f
        enemies.clear()
        hasStarted = true
        showMenu = false
        showGarage = false
    }

    // GAME LOOP
    LaunchedEffect(gameOver, hasStarted) {
        while (hasStarted && !gameOver) {
            delay(16)

            roadOffset = (roadOffset + gameSpeed) % 100f
            spawnTimer++

            if (spawnTimer >= (48 - score / 10).coerceAtLeast(24)) {
                spawnTimer = 0

                enemies.add(
                    EnemyCar(
                        lane = Random.nextInt(3),
                        y = -170f,
                        color = colors.random(),
                        model = VehicleModel.values().random()
                    )
                )
            }

            val iterator = enemies.iterator()

            while (iterator.hasNext()) {
                val enemy = iterator.next()
                enemy.y += gameSpeed

                if (enemy.y > 850f) {
                    iterator.remove()
                    score++
                    gameSpeed = (7f + score * 0.035f).coerceAtMost(18f)
                }
            }

            // COLLISION
            val playerSize = vehicleSize(playerModel)
            val laneWidth = 340f / 3f

            val playerCenter =
                30f + playerLane * laneWidth + laneWidth / 2f

            val playerLeft = playerCenter - playerSize.first / 2f
            val playerRight = playerCenter + playerSize.first / 2f
            val playerTop = 555f
            val playerBottom = playerTop + playerSize.second

            val collision = enemies.any { enemy ->
                val enemySize = vehicleSize(enemy.model)

                val enemyCenter =
                    30f + enemy.lane * laneWidth + laneWidth / 2f

                val enemyLeft = enemyCenter - enemySize.first / 2f
                val enemyRight = enemyCenter + enemySize.first / 2f
                val enemyTop = enemy.y
                val enemyBottom = enemy.y + enemySize.second

                playerLeft + 5f < enemyRight - 5f &&
                        playerRight - 5f > enemyLeft + 5f &&
                        playerTop + 5f < enemyBottom - 5f &&
                        playerBottom - 5f > enemyTop + 5f
            }

            if (collision) {
                gameOver = true
                crashFinished = false
                bestScore = maxOf(bestScore, score)
                preferences.edit().putInt("best_score", bestScore).apply()
            }
        }
    }

    // CRASH ANIMATION
    LaunchedEffect(gameOver) {
        if (gameOver) {
            for (i in 0..35) {
                crashProgress = i / 35f
                delay(20)
            }
            crashFinished = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Grass)
            .pointerInput(gameOver, hasStarted) {
                var totalDrag = 0f
                var moved = false

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        if (hasStarted && !gameOver && !moved) {
                            totalDrag += dragAmount

                            if (abs(totalDrag) > 55f) {
                                if (totalDrag < 0f) {
                                    playerLane = (playerLane - 1).coerceAtLeast(0)
                                } else {
                                    playerLane = (playerLane + 1).coerceAtMost(2)
                                }
                                moved = true
                            }
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        totalDrag = 0f
                        moved = false
                    },
                    onDragCancel = {
                        totalDrag = 0f
                        moved = false
                    }
                )
            }
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {

            val sx = size.width / 400f
            val sy = size.height / 700f

            fun rect(
                color: Color,
                x: Float,
                y: Float,
                w: Float,
                h: Float
            ) {
                drawRect(
                    color = color,
                    topLeft = Offset(x * sx, y * sy),
                    size = Size(w * sx, h * sy)
                )
            }

            fun circle(
                color: Color,
                x: Float,
                y: Float,
                radius: Float
            ) {
                drawCircle(
                    color = color,
                    radius = radius * sx,
                    center = Offset(x * sx, y * sy)
                )
            }

            // BACKGROUND
            rect(Grass, 0f, 0f, 400f, 700f)

            // GRASS
            for (i in 0..17) {
                val y = (i * 52f + roadOffset * 1.3f) % 760f - 40f

                rect(Color(0xFF20563C), 5f, y, 18f, 25f)
                rect(Color(0xFF20563C), 377f, y + 20f, 18f, 25f)

                rect(Color(0xFF34784B), 9f, y - 5f, 7f, 7f)
                rect(Color(0xFF34784B), 384f, y + 15f, 7f, 7f)

                rect(Color(0xFF102E24), 3f, y + 26f, 22f, 3f)
                rect(Color(0xFF102E24), 375f, y + 46f, 22f, 3f)
            }

            // ROAD
            rect(Color(0xFF111722), 25f, 0f, 350f, 700f)
            rect(Road, 30f, 0f, 340f, 700f)

            // CURBS
            for (i in 0..15) {
                val y = (i * 50f + roadOffset) % 760f - 50f

                rect(Color.White, 26f, y, 5f, 25f)
                rect(Color(0xFFE74C3C), 26f, y + 25f, 5f, 25f)

                rect(Color.White, 369f, y, 5f, 25f)
                rect(Color(0xFFE74C3C), 369f, y + 25f, 5f, 25f)
            }

            // ROAD TEXTURE
            for (i in 0..35) {
                val y = (i * 37f + roadOffset * 1.5f) % 720f

                rect(
                    Color.White.copy(alpha = 0.035f),
                    45f + (i % 4) * 75f,
                    y,
                    9f,
                    2f
                )
            }

            // LANE MARKINGS
            for (lane in 1..2) {
                val x = 30f + lane * (340f / 3f)

                for (i in 0..10) {
                    val y = i * 100f + roadOffset - 100f

                    rect(Color(0xFF101722), x - 4f, y, 8f, 50f)
                    rect(Color(0xFFE1E5EA), x - 2f, y, 4f, 48f)
                }
            }

            // STREET LAMPS
            for (i in 0..5) {
                val y = (i * 150f + roadOffset * 1.8f) % 850f - 100f

                rect(Color(0xFF17212A), 14f, y, 4f, 48f)
                rect(Color(0xFF17212A), 9f, y, 14f, 5f)

                circle(
                    Color(0xFFFFD98A).copy(alpha = 0.25f),
                    16f, y + 8f, 14f
                )
                rect(Yellow, 12f, y + 5f, 9f, 5f)

                rect(Color(0xFF17212A), 382f, y + 50f, 4f, 48f)
                rect(Color(0xFF17212A), 377f, y + 50f, 14f, 5f)

                circle(
                    Color(0xFFFFD98A).copy(alpha = 0.25f),
                    384f, y + 58f, 14f
                )
                rect(Yellow, 380f, y + 55f, 9f, 5f)
            }

            // ENEMY VEHICLE RENDERER
            // ============================================================
            // DETAILED VEHICLE RENDERER — ALL TRAFFIC VEHICLES
            // ============================================================

            fun drawVehicle(
                lane: Int,
                y: Float,
                color: Color,
                model: VehicleModel,
                player: Boolean = false
            ) {
                val style = model.style
                val variant = model.variant
                val w = model.width
                val h = model.height

                val laneWidth = 340f / 3f
                val center = 30f + lane * laneWidth + laneWidth / 2f
                val x = center - w / 2f

                val vx = w / 60f
                val vy = h / 100f

                val black = Color(0xFF080C12)
                val dark = Color(0xFF151D27)
                val carbon = Color(0xFF252F3A)
                val metal = Color(0xFF9BA8B5)
                val silver = Color(0xFFD4DCE3)
                val glassBlue = Color(0xFF8CCDE5)
                val glassDark = Color(0xFF102B40)
                val red = Color(0xFFFF293D)
                val white = Color(0xFFFFF8E8)

                val accent = when (variant % 5) {
                    0 -> Color(0xFFB9DFFF)
                    1 -> Color(0xFFFFD782)
                    2 -> Color(0xFFB7F3E5)
                    3 -> Color(0xFFFFA8A8)
                    else -> Color(0xFFE4D5FF)
                }

                fun rr(
                    c: Color,
                    px: Float,
                    py: Float,
                    width: Float,
                    height: Float,
                    radius: Float = 4f
                ) {
                    drawRoundRect(
                        color = c,
                        topLeft = Offset(
                            (x + px * vx) * sx,
                            (y + py * vy) * sy
                        ),
                        size = Size(
                            width * vx * sx,
                            height * vy * sy
                        ),
                        cornerRadius = CornerRadius(radius * vx * sx)
                    )
                }

                fun grad(
                    c1: Color,
                    c2: Color,
                    px: Float,
                    py: Float,
                    width: Float,
                    height: Float,
                    radius: Float = 5f
                ) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(c1, c2),
                            startY = (y + py * vy) * sy,
                            endY = (y + (py + height) * vy) * sy
                        ),
                        topLeft = Offset(
                            (x + px * vx) * sx,
                            (y + py * vy) * sy
                        ),
                        size = Size(
                            width * vx * sx,
                            height * vy * sy
                        ),
                        cornerRadius = CornerRadius(radius * vx * sx)
                    )
                }

                fun ln(
                    c: Color,
                    x1: Float,
                    y1: Float,
                    x2: Float,
                    y2: Float,
                    stroke: Float = 1f
                ) {
                    drawLine(
                        color = c,
                        start = Offset(
                            (x + x1 * vx) * sx,
                            (y + y1 * vy) * sy
                        ),
                        end = Offset(
                            (x + x2 * vx) * sx,
                            (y + y2 * vy) * sy
                        ),
                        strokeWidth = stroke * vx * sx
                    )
                }

                fun dot(
                    c: Color,
                    px: Float,
                    py: Float,
                    radius: Float
                ) {
                    drawCircle(
                        color = c,
                        radius = radius * vx * sx,
                        center = Offset(
                            (x + px * vx) * sx,
                            (y + py * vy) * sy
                        )
                    )
                }

                // --------------------------------------------------------
                // PLAYER SUPERCAR — low, wide, angular Lamborghini-inspired
                // silhouette. This is used for EVERY player-car unlock,
                // including cars that are still locked in the garage.
                // --------------------------------------------------------
                if (player) {
                    val neon = when (variant % 4) {
                        0 -> Color(0xFFB9FFF4)
                        1 -> Color(0xFFFFE18A)
                        2 -> Color(0xFFFFA6C1)
                        else -> Color(0xFFB8D6FF)
                    }

                    // Wide shadow and aggressive low-profile tires
                    rr(Color.Black.copy(alpha = 0.62f), 3f, 5f, 54f, 91f, 13f)
                    rr(Color(0xFF070B11), 0f, 19f, 11f, 25f, 4f)
                    rr(Color(0xFF070B11), 49f, 19f, 11f, 25f, 4f)
                    rr(Color(0xFF070B11), 0f, 66f, 11f, 25f, 4f)
                    rr(Color(0xFF070B11), 49f, 66f, 11f, 25f, 4f)

                    // Forged alloy highlights
                    for (wheelY in listOf(22f, 69f)) {
                        rr(Color(0xFF56616D), 2f, wheelY + 5f, 7f, 15f, 2f)
                        rr(Color(0xFF56616D), 51f, wheelY + 5f, 7f, 15f, 2f)
                        rr(Color(0xFFCAD4DD), 4f, wheelY + 8f, 3f, 9f, 1f)
                        rr(Color(0xFFCAD4DD), 53f, wheelY + 8f, 3f, 9f, 1f)
                        dot(Color(0xFF1A222C), 5.5f, wheelY + 12f, 1.2f)
                        dot(Color(0xFF1A222C), 54.5f, wheelY + 12f, 1.2f)
                    }

                    // Carbon-fibre chassis and sharp wedge-shaped body
                    rr(Color(0xFF05080D), 5f, 2f, 50f, 96f, 12f)
                    grad(Color(0xFFFAFAFA).copy(alpha = 0.10f), Color(0xFF080D15), 7f, 5f, 46f, 89f, 10f)
                    grad(color.copy(alpha = 0.98f), color.copy(alpha = 0.78f), 8f, 4f, 44f, 91f, 10f)

                    // Sharp front hood and angular creases
                    rr(color.copy(alpha = 0.9f), 12f, 5f, 36f, 22f, 8f)
                    ln(Color.White.copy(alpha = 0.62f), 12f, 8f, 25f, 26f, 1.4f)
                    ln(Color.White.copy(alpha = 0.62f), 48f, 8f, 35f, 26f, 1.4f)
                    ln(Color.Black.copy(alpha = 0.38f), 17f, 13f, 25f, 25f, 1.1f)
                    ln(Color.Black.copy(alpha = 0.38f), 43f, 13f, 35f, 25f, 1.1f)
                    rr(Color(0xFF111923), 23f, 10f, 14f, 10f, 3f)
                    for (i in 0..2) {
                        rr(Color(0xFF080D13), 25f + i * 4f, 12f, 2f, 7f, 1f)
                    }

                    // Distinctive Y-shaped LED headlights
                    rr(Color(0xFF111923), 8f, 27f, 17f, 5f, 2f)
                    rr(Color(0xFF111923), 35f, 27f, 17f, 5f, 2f)
                    ln(Color(0xFFE9FFFF), 10f, 28f, 21f, 30f, 1.8f)
                    ln(Color(0xFFE9FFFF), 50f, 28f, 39f, 30f, 1.8f)
                    ln(neon, 14f, 28f, 19f, 31f, 1.2f)
                    ln(neon, 46f, 28f, 41f, 31f, 1.2f)

                    // Deep front intakes and carbon splitter
                    rr(Color(0xFF070B10), 8f, 33f, 13f, 7f, 2f)
                    rr(Color(0xFF070B10), 39f, 33f, 13f, 7f, 2f)
                    rr(Color(0xFF101923), 23f, 34f, 14f, 6f, 2f)
                    rr(Color(0xFF05080D), 5f, 40f, 50f, 4f, 1f)
                    rr(Color(0xFF8A9AA8), 10f, 41f, 9f, 1f, 1f)
                    rr(Color(0xFF8A9AA8), 41f, 41f, 9f, 1f, 1f)

                    // Low fighter-jet cockpit
                    grad(Color(0xFF9EDCF0), Color(0xFF10283C), 14f, 43f, 32f, 24f, 10f)
                    rr(Color(0xFF101923), 13f, 43f, 34f, 3f, 2f)
                    rr(Color(0xFF101923), 13f, 63f, 34f, 3f, 2f)
                    ln(Color.White.copy(alpha = 0.72f), 17f, 46f, 24f, 60f, 1.3f)
                    ln(Color.White.copy(alpha = 0.30f), 22f, 46f, 27f, 59f, 0.9f)
                    ln(Color(0xFF77BBD5), 43f, 47f, 37f, 60f, 0.9f)
                    rr(Color(0xFF080E17), 28f, 45f, 4f, 18f, 2f)

                    // Sculpted side blades and large air intakes
                    rr(Color(0xFF080D14), 7f, 48f, 7f, 20f, 3f)
                    rr(Color(0xFF080D14), 46f, 48f, 7f, 20f, 3f)
                    rr(Color(0xFF657786), 8f, 52f, 4f, 2f, 1f)
                    rr(Color(0xFF657786), 8f, 57f, 4f, 2f, 1f)
                    rr(Color(0xFF657786), 48f, 52f, 4f, 2f, 1f)
                    rr(Color(0xFF657786), 48f, 57f, 4f, 2f, 1f)
                    ln(Color.White.copy(alpha = 0.52f), 9f, 45f, 14f, 72f, 1f)
                    ln(Color.White.copy(alpha = 0.52f), 51f, 45f, 46f, 72f, 1f)

                    // Rear engine cover, vents and aerodynamic deck
                    rr(Color(0xFF111923), 15f, 68f, 30f, 14f, 4f)
                    rr(Color(0xFF263440), 18f, 70f, 24f, 9f, 3f)
                    for (i in 0..4) {
                        rr(Color(0xFF080D13), 20f + i * 4f, 71f, 2f, 7f, 1f)
                    }
                    rr(Color(0xFF080D13), 9f, 81f, 42f, 5f, 2f)

                    // Full-width rear light bar, diffuser and exhausts
                    rr(Color(0xFF250E18), 9f, 85f, 42f, 5f, 2f)
                    rr(Color(0xFFFF2948), 12f, 86f, 36f, 2f, 1f)
                    rr(Color(0xFFFFA1AD), 17f, 86f, 26f, 1f, 1f)
                    rr(Color(0xFF05080D), 7f, 91f, 46f, 6f, 2f)
                    for (i in 0..4) {
                        rr(Color(0xFF687887), 14f + i * 7f, 92f, 2f, 4f, 1f)
                    }
                    rr(Color(0xFFB6C3CE), 12f, 91f, 6f, 3f, 1f)
                    rr(Color(0xFFB6C3CE), 42f, 91f, 6f, 3f, 1f)

                    // Signature center stripe and glowing side skirts
                    if (variant % 3 != 1) {
                        rr(neon.copy(alpha = 0.72f), 29f, 7f, 2f, 18f, 1f)
                    }
                    rr(neon.copy(alpha = 0.75f), 7f, 72f, 3f, 12f, 1f)
                    rr(neon.copy(alpha = 0.75f), 50f, 72f, 3f, 12f, 1f)
                    rr(Color.White.copy(alpha = 0.28f), 12f, 5f, 2f, 20f, 1f)
                    rr(Color.White.copy(alpha = 0.20f), 46f, 5f, 2f, 20f, 1f)
                    return
                }

                // --------------------------------------------------------
                // MOTORCYCLES
                // --------------------------------------------------------

                if (style == VehicleStyle.MOTORCYCLE) {

                    val wheelWidth = if (variant % 2 == 0) 13f else 15f
                    val wheelX = (60f - wheelWidth) / 2f

                    // Ground shadow
                    rr(Color.Black.copy(alpha = 0.5f), 16f, 5f, 28f, 91f, 12f)

                    // Tires
                    rr(black, wheelX, 2f, wheelWidth, 27f, 7f)
                    rr(black, wheelX, 72f, wheelWidth, 26f, 7f)

                    rr(Color(0xFF525D68), wheelX + 3f, 7f, wheelWidth - 6f, 17f, 3f)
                    rr(Color(0xFF525D68), wheelX + 3f, 77f, wheelWidth - 6f, 16f, 3f)

                    // Wheel hubs
                    rr(metal, 26f, 12f, 8f, 7f, 3f)
                    rr(metal, 26f, 81f, 8f, 7f, 3f)
                    dot(Color(0xFF202833), 30f, 15.5f, 2f)
                    dot(Color(0xFF202833), 30f, 84.5f, 2f)

                    // Front fork
                    rr(Color(0xFFB4BEC7), 22f, 17f, 3f, 30f, 1f)
                    rr(Color(0xFFB4BEC7), 35f, 17f, 3f, 30f, 1f)

                    // Rear swingarm
                    rr(Color(0xFF747F8B), 22f, 57f, 3f, 23f, 1f)
                    rr(Color(0xFF747F8B), 35f, 57f, 3f, 23f, 1f)

                    // Engine
                    rr(Color(0xFF303B46), 19f, 43f, 22f, 24f, 5f)
                    rr(Color(0xFF818D98), 22f, 46f, 16f, 17f, 4f)

                    for (i in 0..3) {
                        rr(Color(0xFF252E38), 24f, 48f + i * 4f, 12f, 2f, 1f)
                    }

                    // Exhaust
                    rr(Color(0xFF929DA7), 39f, 55f, 7f, 19f, 3f)
                    rr(Color(0xFF262F39), 40f, 59f, 5f, 12f, 2f)

                    // Different motorcycle designs
                    when (variant) {
                        0 -> {
                            // Sport bike
                            grad(color.copy(alpha = 0.95f), color.copy(alpha = 0.65f),
                                11f, 24f, 38f, 31f, 12f)

                            grad(Color(0xFFB9E5F5), glassDark, 19f, 17f, 22f, 15f, 7f)

                            rr(dark, 18f, 31f, 24f, 14f, 5f)
                            rr(color, 12f, 43f, 36f, 10f, 5f)
                            rr(carbon, 20f, 63f, 20f, 12f, 5f)

                            ln(silver, 13f, 37f, 22f, 44f)
                            ln(silver, 47f, 37f, 38f, 44f)
                        }

                        1 -> {
                            // Naked street bike
                            grad(color, color.copy(alpha = 0.55f), 15f, 32f, 30f, 25f, 8f)
                            rr(carbon, 20f, 24f, 20f, 22f, 6f)
                            rr(Color(0xFFB7DDEB), 23f, 20f, 14f, 9f, 4f)
                            rr(dark, 18f, 58f, 24f, 15f, 6f)

                            ln(metal, 17f, 36f, 12f, 54f)
                            ln(metal, 43f, 36f, 48f, 54f)
                        }

                        2 -> {
                            // Cruiser
                            rr(dark, 16f, 34f, 28f, 34f, 10f)
                            grad(color, color.copy(alpha = 0.65f), 13f, 42f, 34f, 20f, 9f)

                            rr(metal, 12f, 30f, 36f, 4f, 2f)
                            rr(metal, 19f, 67f, 22f, 5f, 2f)
                            rr(Color(0xFFFFE5B2), 23f, 24f, 14f, 10f, 5f)
                        }

                        3 -> {
                            // Scooter
                            grad(color, color.copy(alpha = 0.6f), 15f, 27f, 30f, 48f, 10f)
                            rr(dark, 19f, 41f, 22f, 25f, 8f)
                            grad(Color(0xFFB7DFEE), glassDark, 21f, 22f, 18f, 15f, 7f)
                            rr(Color(0xFFADB9C3), 16f, 68f, 28f, 5f, 2f)
                        }

                        4 -> {
                            // Touring bike
                            grad(color, color.copy(alpha = 0.65f), 10f, 26f, 40f, 45f, 12f)
                            grad(Color(0xFFB9DEEE), glassDark, 16f, 19f, 28f, 22f, 9f)
                            rr(dark, 17f, 48f, 26f, 26f, 8f)
                            rr(metal, 12f, 64f, 36f, 8f, 3f)
                        }

                        5 -> {
                            // Dirt bike
                            rr(dark, 21f, 27f, 18f, 47f, 5f)
                            grad(color, color.copy(alpha = 0.5f), 15f, 35f, 30f, 34f, 7f)
                            rr(metal, 25f, 19f, 10f, 8f, 3f)
                            ln(silver, 20f, 37f, 13f, 59f)
                            ln(silver, 40f, 37f, 47f, 59f)
                        }

                        6 -> {
                            // Retro motorcycle
                            rr(dark, 17f, 30f, 26f, 43f, 8f)
                            grad(color, color.copy(alpha = 0.6f), 15f, 40f, 30f, 25f, 9f)
                            rr(Color(0xFFFFE5A7), 22f, 23f, 16f, 12f, 6f)
                            rr(metal, 15f, 65f, 30f, 5f, 3f)
                        }

                        else -> {
                            // Streetfighter
                            rr(dark, 16f, 29f, 28f, 44f, 7f)
                            grad(color, color.copy(alpha = 0.55f), 12f, 35f, 36f, 31f, 9f)
                            rr(Color(0xFF9DD8E9), 21f, 22f, 18f, 10f, 4f)
                            rr(metal, 12f, 29f, 36f, 4f, 2f)
                        }
                    }

                    // Handlebars
                    rr(metal, 8f, 23f, 44f, 3f, 2f)
                    rr(black, 7f, 19f, 9f, 7f, 3f)
                    rr(black, 44f, 19f, 9f, 7f, 3f)

                    // Mirrors
                    rr(Color(0xFF111820), 8f, 15f, 8f, 5f, 2f)
                    rr(Color(0xFF111820), 44f, 15f, 8f, 5f, 2f)
                    rr(Color(0xFF9BA8B5), 10f, 16f, 4f, 2f, 1f)
                    rr(Color(0xFF9BA8B5), 46f, 16f, 4f, 2f, 1f)

                    // Headlight
                    rr(Color(0xFFFFE8A9), 24f, 14f, 12f, 7f, 3f)
                    rr(white, 27f, 15f, 6f, 3f, 2f)

                    // Rear light
                    rr(red, 24f, 92f, 12f, 4f, 2f)

                    // Mechanical detail
                    for (i in 0..2) {
                        ln(Color.White.copy(alpha = 0.25f), 22f, 47f + i * 5f, 38f, 47f + i * 5f)
                    }

                    return
                }

                // --------------------------------------------------------
                // BODY DIMENSIONS
                // --------------------------------------------------------

                val bodyLeft = when (style) {
                    VehicleStyle.SPORT -> 7f + (variant % 3)
                    VehicleStyle.VAN -> 5f
                    VehicleStyle.TRUCK -> 6f
                    VehicleStyle.SUV -> 6f
                    VehicleStyle.COMPACT -> 9f
                    else -> 8f
                }

                val bodyRight = 60f - bodyLeft
                val bodyWidth = bodyRight - bodyLeft

                val bodyRadius = when (style) {
                    VehicleStyle.SPORT -> 13f
                    VehicleStyle.VAN -> 6f
                    VehicleStyle.TRUCK -> 7f
                    VehicleStyle.SUV -> 9f
                    VehicleStyle.COMPACT -> 9f
                    else -> 10f
                }

                // --------------------------------------------------------
                // SHADOW, NEON UNDERGLOW AND UNDERBODY
                // --------------------------------------------------------

                rr(Color.Black.copy(alpha = 0.58f),
                    bodyLeft - 4f, 5f, bodyWidth + 8f, 94f, bodyRadius + 3f)

                if (player) {
                    rr(color.copy(alpha = 0.20f), bodyLeft - 2f, 22f, bodyWidth + 4f, 52f, bodyRadius)
                    rr(color.copy(alpha = 0.32f), bodyLeft + 3f, 12f, bodyWidth - 6f, 2f, 1f)
                    rr(Color.White.copy(alpha = 0.18f), bodyLeft + 3f, 8f, 3f, 24f, 2f)
                }

                rr(Color(0xFF10151C), bodyLeft, 5f, bodyWidth, 91f, bodyRadius)

                // --------------------------------------------------------
                // WHEELS
                // --------------------------------------------------------

                val wheelPositions = when (style) {
                    VehicleStyle.SPORT -> listOf(17f, 68f)
                    VehicleStyle.TRUCK -> listOf(13f, 73f)
                    VehicleStyle.VAN -> listOf(15f, 73f)
                    VehicleStyle.SUV -> listOf(15f, 72f)
                    else -> listOf(17f, 71f)
                }

                val tireWidth = when (style) {
                    VehicleStyle.SPORT -> 12f
                    VehicleStyle.SUV -> 12f
                    VehicleStyle.TRUCK -> 13f
                    else -> 11f
                }

                val tireHeight = when (style) {
                    VehicleStyle.SPORT -> 24f
                    VehicleStyle.TRUCK -> 25f
                    else -> 23f
                }

                for (wheelY in wheelPositions) {
                    // Tire
                    rr(black, 0f, wheelY, tireWidth, tireHeight, 4f)
                    rr(black, 60f - tireWidth, wheelY, tireWidth, tireHeight, 4f)

                    // Tire edges
                    rr(Color(0xFF3B4652), 2f, wheelY + 3f, 2f, 17f, 1f)
                    rr(Color(0xFF3B4652), 56f, wheelY + 3f, 2f, 17f, 1f)

                    // Alloy rims
                    rr(Color(0xFFBBC5CD), 3f, wheelY + 5f, 6f, 13f, 2f)
                    rr(Color(0xFFBBC5CD), 51f, wheelY + 5f, 6f, 13f, 2f)

                    // Inner hubs
                    rr(Color(0xFF414D59), 5f, wheelY + 8f, 2f, 7f, 1f)
                    rr(Color(0xFF414D59), 53f, wheelY + 8f, 2f, 7f, 1f)

                    // Rim highlights
                    ln(Color.White.copy(alpha = 0.55f), 4f, wheelY + 7f, 7f, wheelY + 7f, 0.8f)
                    ln(Color.White.copy(alpha = 0.55f), 53f, wheelY + 7f, 56f, wheelY + 7f, 0.8f)

                    // Brake discs
                    rr(Color(0xFF7D8995), 4f, wheelY + 11f, 4f, 1.5f, 1f)
                    rr(Color(0xFF7D8995), 52f, wheelY + 11f, 4f, 1.5f, 1f)

                    // Wheel arches
                    rr(Color(0xFF111821), 7f, wheelY - 2f, 8f, 3f, 2f)
                    rr(Color(0xFF111821), 45f, wheelY - 2f, 8f, 3f, 2f)
                }

                // --------------------------------------------------------
                // MAIN BODY
                // --------------------------------------------------------

                rr(Color(0xFF05080D),
                    bodyLeft, 2f, bodyWidth, 96f, bodyRadius)

                grad(
                    color.copy(alpha = 0.96f),
                    color,
                    bodyLeft + 2f, 4f,
                    bodyWidth - 4f, 92f,
                    bodyRadius
                )

                // Outer body reflections
                rr(Color.White.copy(alpha = 0.12f),
                    bodyLeft + 2f, 7f, 2f, 75f, 1f)

                rr(Color.Black.copy(alpha = 0.12f),
                    bodyRight - 4f, 8f, 2f, 75f, 1f)

                // --------------------------------------------------------
                // STYLE-SPECIFIC BODY DESIGNS
                // --------------------------------------------------------

                when (style) {

                    // ====================================================
                    // SPORTS CARS
                    // ====================================================

                    VehicleStyle.SPORT -> {

                        // Long aerodynamic hood
                        grad(
                            color.copy(alpha = 0.98f),
                            color.copy(alpha = 0.68f),
                            10f, 4f, 40f, 29f, 12f
                        )

                        // Hood sculpting
                        ln(Color.White.copy(alpha = 0.48f), 14f, 9f, 22f, 25f, 1.2f)
                        ln(Color.White.copy(alpha = 0.48f), 46f, 9f, 38f, 25f, 1.2f)

                        ln(Color(0xFF633E0B).copy(alpha = 0.5f), 12f, 20f, 23f, 29f)
                        ln(Color(0xFF633E0B).copy(alpha = 0.5f), 48f, 20f, 37f, 29f)

                        // Central hood ridge
                        rr(Color.White.copy(alpha = 0.2f), 28f, 8f, 3f, 16f, 1f)

                        // Hood vents
                        for (i in 0..2) {
                            rr(Color(0xFF171D25), 15f + i * 4f, 18f, 2f, 5f, 1f)
                            rr(Color(0xFF171D25), 37f + i * 4f, 18f, 2f, 5f, 1f)
                        }

                        // Front aerodynamic air intakes
                        rr(black, 8f, 27f, 13f, 5f, 2f)
                        rr(black, 39f, 27f, 13f, 5f, 2f)

                        rr(Color(0xFF5D6976), 10f, 28f, 9f, 1f, 1f)
                        rr(Color(0xFF5D6976), 41f, 28f, 9f, 1f, 1f)

                        // Front splitter
                        rr(Color(0xFF090D13), 6f, 31f, 48f, 4f, 1f)
                        rr(Color(0xFF687582), 12f, 32f, 8f, 1f, 1f)
                        rr(Color(0xFF687582), 40f, 32f, 8f, 1f, 1f)

                        // Low sports cabin
                        grad(
                            color.copy(alpha = 0.93f),
                            color.copy(alpha = 0.64f),
                            13f, 43f, 34f, 21f, 9f
                        )

                        // Windshield
                        grad(
                            glassBlue,
                            glassDark,
                            14f, 32f, 32f, 15f, 7f
                        )

                        // Glass reflections
                        ln(Color.White.copy(alpha = 0.7f), 18f, 34f, 24f, 45f, 1.2f)
                        ln(Color.White.copy(alpha = 0.25f), 23f, 34f, 28f, 44f)
                        ln(Color(0xFF5DA3C1), 42f, 35f, 37f, 44f)

                        // Cabin outline
                        rr(Color(0xFF101923), 12f, 31f, 36f, 2f, 1f)
                        rr(Color(0xFF101923), 12f, 47f, 36f, 2f, 1f)

                        // Roof
                        grad(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.7f),
                            17f, 48f, 26f, 13f, 6f
                        )

                        // Rear engine glass
                        grad(
                            Color(0xFF416A82),
                            Color(0xFF101E2B),
                            17f, 63f, 26f, 12f, 4f
                        )

                        // Engine-cover vents
                        for (i in 0..4) {
                            rr(Color(0xFF111820), 19f + i * 5f, 65f, 2f, 7f, 1f)
                        }

                        // Side air intakes
                        rr(Color(0xFF10161F), 7f, 48f, 8f, 17f, 3f)
                        rr(Color(0xFF10161F), 45f, 48f, 8f, 17f, 3f)

                        for (i in 0..2) {
                            rr(Color(0xFF65717E), 9f, 51f + i * 4f, 4f, 1f, 1f)
                            rr(Color(0xFF65717E), 47f, 51f + i * 4f, 4f, 1f, 1f)
                        }

                        // Sculpted sides
                        ln(Color.White.copy(alpha = 0.45f), 9f, 39f, 12f, 64f, 1f)
                        ln(Color.White.copy(alpha = 0.45f), 51f, 39f, 48f, 64f, 1f)

                        ln(Color(0xFF754A0B).copy(alpha = 0.55f), 14f, 54f, 20f, 65f)
                        ln(Color(0xFF754A0B).copy(alpha = 0.55f), 46f, 54f, 40f, 65f)

                        // Rear deck
                        grad(
                            color.copy(alpha = 0.84f),
                            color.copy(alpha = 0.55f),
                            11f, 74f, 38f, 12f, 5f
                        )

                        // Rear diffuser
                        rr(black, 8f, 84f, 44f, 7f, 2f)

                        for (i in 0..4) {
                            rr(Color(0xFF697481), 14f + i * 7f, 85f, 2f, 5f, 1f)
                        }

                        // Exhausts
                        rr(black, 14f, 82f, 7f, 4f, 1f)
                        rr(black, 39f, 82f, 7f, 4f, 1f)
                        rr(silver, 15f, 83f, 5f, 1f, 1f)
                        rr(silver, 40f, 83f, 5f, 1f, 1f)

                        // Rear lights
                        rr(Color(0xFF1B1119), 9f, 77f, 15f, 5f, 2f)
                        rr(Color(0xFF1B1119), 36f, 77f, 15f, 5f, 2f)
                        rr(red, 11f, 78f, 11f, 2f, 1f)
                        rr(red, 38f, 78f, 11f, 2f, 1f)

                        // Rear spoiler variants
                        if (variant % 3 != 1) {
                            rr(Color(0xFF151B23), 6f, 88f, 48f, 3f, 1f)
                            rr(Color(0xFF505D6A), 11f, 88f, 38f, 1f, 1f)
                            rr(Color(0xFF202A35), 13f, 84f, 4f, 5f, 1f)
                            rr(Color(0xFF202A35), 43f, 84f, 4f, 5f, 1f)
                        }

                        // Racing stripes on selected models
                        if (variant % 3 == 0) {
                            rr(Color.White.copy(alpha = 0.4f), 27f, 7f, 2f, 18f, 1f)
                            rr(Color.White.copy(alpha = 0.25f), 27f, 51f, 2f, 10f, 1f)
                        }
                    }

                    // ====================================================
                    // COMPACT CARS
                    // ====================================================

                    VehicleStyle.COMPACT -> {

                        // Rounded hood
                        grad(
                            color.copy(alpha = 0.95f),
                            color.copy(alpha = 0.68f),
                            12f, 5f, 36f, 24f, 8f
                        )

                        // Hood details
                        ln(Color.White.copy(alpha = 0.4f), 17f, 9f, 23f, 25f)
                        ln(Color.White.copy(alpha = 0.4f), 43f, 9f, 37f, 25f)

                        // Windshield
                        grad(
                            glassBlue,
                            glassDark,
                            13f, 29f, 34f, 15f, 6f
                        )

                        // Cabin
                        grad(
                            color.copy(alpha = 0.96f),
                            color.copy(alpha = 0.7f),
                            10f, 42f, 40f, 29f, 8f
                        )

                        // Front side windows
                        grad(Color(0xFF85BED4), glassDark, 11f, 44f, 7f, 17f, 2f)
                        grad(Color(0xFF85BED4), glassDark, 42f, 44f, 7f, 17f, 2f)

                        // Rear window
                        grad(
                            Color(0xFF6B9DB4),
                            Color(0xFF142A3A),
                            15f, 63f, 30f, 9f, 3f
                        )

                        // Window separators
                        rr(Color(0xFF131A23), 28f, 44f, 3f, 19f, 1f)

                        // Door lines
                        ln(Color.Black.copy(alpha = 0.35f), 11f, 49f, 12f, 77f)
                        ln(Color.Black.copy(alpha = 0.35f), 49f, 49f, 48f, 77f)

                        // Door handles
                        rr(silver, 11f, 55f, 4f, 2f, 1f)
                        rr(silver, 45f, 55f, 4f, 2f, 1f)

                        // Rear trunk
                        grad(
                            color.copy(alpha = 0.85f),
                            color.copy(alpha = 0.56f),
                            12f, 73f, 36f, 14f, 5f
                        )

                        // Rear bumper
                        rr(Color(0xFF333D48), 10f, 88f, 40f, 6f, 2f)

                        // Rear lights
                        rr(red, 10f, 82f, 10f, 5f, 2f)
                        rr(red, 40f, 82f, 10f, 5f, 2f)
                        rr(Color(0xFFFF9D9D), 12f, 83f, 5f, 2f, 1f)
                        rr(Color(0xFFFF9D9D), 43f, 83f, 5f, 2f, 1f)

                        // Front bumper and grille
                        rr(Color(0xFF303944), 10f, 3f, 40f, 5f, 2f)
                        rr(black, 19f, 5f, 22f, 3f, 1f)
                        rr(metal, 22f, 6f, 16f, 1f, 1f)

                        // Headlights
                        rr(white, 11f, 8f, 10f, 5f, 2f)
                        rr(white, 39f, 8f, 10f, 5f, 2f)
                        rr(Color(0xFFB4E9FF), 13f, 9f, 6f, 2f, 1f)
                        rr(Color(0xFFB4E9FF), 41f, 9f, 6f, 2f, 1f)

                        if (variant % 2 == 0) {
                            // Roof rails
                            rr(Color(0xFFB6C0C9), 15f, 43f, 2f, 23f, 1f)
                            rr(Color(0xFFB6C0C9), 43f, 43f, 2f, 23f, 1f)
                        } else {
                            // Roof accent
                            rr(Color.White.copy(alpha = 0.25f), 27f, 47f, 5f, 14f, 2f)
                        }
                    }

                    // ====================================================
                    // SEDANS
                    // ====================================================

                    VehicleStyle.SEDAN -> {

                        // Long premium hood
                        grad(
                            color.copy(alpha = 0.97f),
                            color.copy(alpha = 0.67f),
                            10f, 5f, 40f, 27f, 8f
                        )

                        // Hood creases
                        ln(Color.White.copy(alpha = 0.45f), 14f, 9f, 23f, 28f)
                        ln(Color.White.copy(alpha = 0.45f), 46f, 9f, 37f, 28f)

                        // Hood vents
                        if (variant % 3 == 0) {
                            rr(Color(0xFF181E27), 16f, 17f, 7f, 3f, 1f)
                            rr(Color(0xFF181E27), 37f, 17f, 7f, 3f, 1f)
                        }

                        // Windshield
                        grad(
                            glassBlue,
                            glassDark,
                            13f, 31f, 34f, 15f, 5f
                        )

                        // Main passenger cabin
                        val roofWidth = if (variant % 2 == 0) 34f else 38f
                        val roofX = (60f - roofWidth) / 2f

                        grad(
                            color.copy(alpha = 0.96f),
                            color.copy(alpha = 0.72f),
                            roofX, 43f, roofWidth, 22f, 6f
                        )

                        // Side windows
                        grad(Color(0xFF91C8DD), glassDark, 10f, 44f, 8f, 17f, 2f)
                        grad(Color(0xFF91C8DD), glassDark, 42f, 44f, 8f, 17f, 2f)

                        // Rear windows
                        grad(
                            Color(0xFF6FA5BC),
                            Color(0xFF142B3C),
                            15f, 63f, 30f, 12f, 4f
                        )

                        // B-pillars
                        rr(Color(0xFF121923), 17f, 44f, 2f, 18f, 1f)
                        rr(Color(0xFF121923), 41f, 44f, 2f, 18f, 1f)

                        // Window reflections
                        ln(Color.White.copy(alpha = 0.5f), 14f, 34f, 20f, 43f)
                        ln(Color.White.copy(alpha = 0.35f), 45f, 34f, 40f, 43f)

                        // Door panels
                        ln(Color.Black.copy(alpha = 0.28f), 10f, 48f, 11f, 77f)
                        ln(Color.Black.copy(alpha = 0.28f), 50f, 48f, 49f, 77f)

                        if (variant % 2 == 0) {
                            // Four-door version
                            ln(Color.Black.copy(alpha = 0.35f), 21f, 45f, 21f, 75f)
                            ln(Color.Black.copy(alpha = 0.35f), 39f, 45f, 39f, 75f)

                            rr(silver, 12f, 54f, 4f, 2f, 1f)
                            rr(silver, 44f, 54f, 4f, 2f, 1f)
                            rr(silver, 12f, 68f, 4f, 2f, 1f)
                            rr(silver, 44f, 68f, 4f, 2f, 1f)
                        } else {
                            // Coupe version
                            ln(Color.Black.copy(alpha = 0.35f), 17f, 45f, 15f, 76f)
                            ln(Color.Black.copy(alpha = 0.35f), 43f, 45f, 45f, 76f)

                            rr(silver, 12f, 59f, 4f, 2f, 1f)
                            rr(silver, 44f, 59f, 4f, 2f, 1f)
                        }

                        // Rear trunk
                        grad(
                            color.copy(alpha = 0.86f),
                            color.copy(alpha = 0.58f),
                            12f, 75f, 36f, 13f, 5f
                        )

                        // Rear deck line
                        rr(Color.Black.copy(alpha = 0.2f), 14f, 76f, 32f, 1f, 1f)

                        // Rear bumper
                        rr(Color(0xFF343E49), 10f, 89f, 40f, 5f, 2f)

                        // Tail lights
                        rr(Color(0xFF35141C), 9f, 83f, 14f, 5f, 2f)
                        rr(Color(0xFF35141C), 37f, 83f, 14f, 5f, 2f)
                        rr(red, 11f, 84f, 10f, 2f, 1f)
                        rr(red, 39f, 84f, 10f, 2f, 1f)

                        // Front grille
                        rr(Color(0xFF252E38), 17f, 5f, 26f, 5f, 2f)
                        rr(black, 20f, 6f, 20f, 3f, 1f)

                        for (i in 0..3) {
                            rr(Color(0xFF697783), 22f + i * 5f, 7f, 1f, 1f, 1f)
                        }

                        // Headlights
                        rr(white, 9f, 8f, 13f, 5f, 2f)
                        rr(white, 38f, 8f, 13f, 5f, 2f)
                        rr(Color(0xFFB7E9FF), 12f, 9f, 7f, 2f, 1f)
                        rr(Color(0xFFB7E9FF), 41f, 9f, 7f, 2f, 1f)

                        // Mirror indicators
                        rr(Color(0xFFFFD879), 4f, 36f, 5f, 2f, 1f)
                        rr(Color(0xFFFFD879), 51f, 36f, 5f, 2f, 1f)
                    }

                    // ====================================================
                    // SUVs
                    // ====================================================

                    VehicleStyle.SUV -> {

                        // Large hood
                        grad(
                            color.copy(alpha = 0.97f),
                            color.copy(alpha = 0.7f),
                            9f, 5f, 42f, 27f, 8f
                        )

                        // Raised hood creases
                        ln(Color.White.copy(alpha = 0.42f), 13f, 8f, 22f, 27f)
                        ln(Color.White.copy(alpha = 0.42f), 47f, 8f, 38f, 27f)

                        // Front windshield
                        grad(
                            glassBlue,
                            glassDark,
                            11f, 30f, 38f, 15f, 5f
                        )

                        // Tall cabin
                        grad(
                            color.copy(alpha = 0.98f),
                            color.copy(alpha = 0.7f),
                            8f, 43f, 44f, 29f, 6f
                        )

                        // Large side windows
                        grad(Color(0xFF8FC6DD), glassDark, 10f, 45f, 10f, 20f, 3f)
                        grad(Color(0xFF8FC6DD), glassDark, 40f, 45f, 10f, 20f, 3f)

                        // Rear side windows
                        grad(Color(0xFF6599B0), Color(0xFF122638), 12f, 66f, 12f, 9f, 2f)
                        grad(Color(0xFF6599B0), Color(0xFF122638), 36f, 66f, 12f, 9f, 2f)

                        // Window pillars
                        rr(Color(0xFF121923), 19f, 44f, 2f, 29f, 1f)
                        rr(Color(0xFF121923), 39f, 44f, 2f, 29f, 1f)

                        // Roof
                        grad(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.7f),
                            10f, 41f, 40f, 5f, 2f
                        )

                        // Roof rails
                        rr(Color(0xFFC5CED5), 13f, 40f, 3f, 28f, 1f)
                        rr(Color(0xFFC5CED5), 44f, 40f, 3f, 28f, 1f)
                        rr(Color(0xFF515F6B), 13f, 42f, 3f, 2f, 1f)
                        rr(Color(0xFF515F6B), 44f, 42f, 3f, 2f, 1f)

                        // Door lines
                        ln(Color.Black.copy(alpha = 0.3f), 9f, 49f, 9f, 79f)
                        ln(Color.Black.copy(alpha = 0.3f), 51f, 49f, 51f, 79f)
                        ln(Color.Black.copy(alpha = 0.28f), 21f, 45f, 21f, 80f)
                        ln(Color.Black.copy(alpha = 0.28f), 39f, 45f, 39f, 80f)

                        // Door handles
                        rr(silver, 10f, 57f, 5f, 2f, 1f)
                        rr(silver, 45f, 57f, 5f, 2f, 1f)
                        rr(silver, 10f, 70f, 5f, 2f, 1f)
                        rr(silver, 45f, 70f, 5f, 2f, 1f)

                        // Rear tailgate
                        grad(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.62f),
                            10f, 75f, 40f, 13f, 4f
                        )

                        // Rear window
                        grad(Color(0xFF6596AC), glassDark, 15f, 76f, 30f, 8f, 3f)

                        // Rear spare wheel on selected SUVs
                        if (variant % 3 == 0) {
                            rr(Color(0xFF101820), 23f, 82f, 14f, 11f, 5f)
                            rr(Color(0xFF8D9BA8), 26f, 84f, 8f, 6f, 3f)
                        }

                        // Bumpers
                        rr(Color(0xFF687581), 8f, 3f, 44f, 5f, 2f)
                        rr(Color(0xFF687581), 8f, 92f, 44f, 5f, 2f)

                        // Front grille
                        rr(black, 17f, 5f, 26f, 6f, 2f)

                        for (i in 0..4) {
                            rr(metal, 20f + i * 4f, 7f, 2f, 2f, 1f)
                        }

                        // Headlights
                        rr(white, 9f, 8f, 12f, 5f, 2f)
                        rr(white, 39f, 8f, 12f, 5f, 2f)

                        // Fog lights
                        rr(Color(0xFFFFE8AF), 10f, 17f, 6f, 4f, 2f)
                        rr(Color(0xFFFFE8AF), 44f, 17f, 6f, 4f, 2f)

                        // Rear lights
                        rr(Color(0xFF31151D), 9f, 86f, 10f, 5f, 2f)
                        rr(Color(0xFF31151D), 41f, 86f, 10f, 5f, 2f)
                        rr(red, 11f, 87f, 6f, 2f, 1f)
                        rr(red, 43f, 87f, 6f, 2f, 1f)

                        // Fender details
                        rr(Color(0xFF161D26), 6f, 16f, 3f, 25f, 1f)
                        rr(Color(0xFF161D26), 51f, 16f, 3f, 25f, 1f)
                    }

                    // ====================================================
                    // VANS
                    // ====================================================

                    VehicleStyle.VAN -> {

                        // Boxy body structure
                        grad(
                            color.copy(alpha = 0.98f),
                            color.copy(alpha = 0.72f),
                            6f, 3f, 48f, 94f, 6f
                        )

                        // Roof edge
                        rr(Color.White.copy(alpha = 0.25f), 9f, 5f, 42f, 2f, 1f)

                        // Large front windshield
                        grad(
                            Color(0xFFB1DCEB),
                            Color(0xFF24485E),
                            11f, 8f, 38f, 19f, 4f
                        )

                        // Windshield reflection
                        ln(Color.White.copy(alpha = 0.65f), 15f, 10f, 23f, 25f, 1.3f)
                        ln(Color.White.copy(alpha = 0.25f), 20f, 10f, 26f, 24f)

                        // Dashboard
                        rr(Color(0xFF17232E), 13f, 25f, 34f, 3f, 1f)

                        // Cabin / passenger windows
                        if (variant % 2 == 0) {

                            grad(
                                Color(0xFF8BC5DC),
                                Color(0xFF17354A),
                                10f, 34f, 40f, 31f, 3f
                            )

                            // Window frames
                            rr(Color(0xFF18222C), 28f, 34f, 3f, 31f, 1f)
                            rr(Color(0xFF18222C), 10f, 48f, 40f, 2f, 1f)

                            // Reflections
                            ln(Color.White.copy(alpha = 0.45f), 13f, 37f, 19f, 61f)
                            ln(Color.White.copy(alpha = 0.25f), 34f, 38f, 39f, 60f)

                        } else {

                            // Cargo van panels
                            rr(Color(0xFF303B46), 10f, 34f, 40f, 35f, 3f)

                            rr(color.copy(alpha = 0.7f), 13f, 37f, 34f, 29f, 2f)

                            // Cargo panel lines
                            ln(Color.Black.copy(alpha = 0.35f), 13f, 38f, 13f, 66f)
                            ln(Color.Black.copy(alpha = 0.35f), 47f, 38f, 47f, 66f)

                            for (i in 0..3) {
                                ln(
                                    Color.White.copy(alpha = 0.13f),
                                    15f,
                                    42f + i * 5f,
                                    45f,
                                    42f + i * 5f
                                )
                            }
                        }

                        // Sliding door
                        ln(Color.Black.copy(alpha = 0.35f), 8f, 35f, 8f, 77f)
                        ln(Color.Black.copy(alpha = 0.35f), 50f, 35f, 50f, 77f)

                        // Door handle and sliding rail
                        rr(silver, 44f, 50f, 5f, 2f, 1f)
                        rr(Color(0xFF84919D), 11f, 68f, 37f, 1f, 1f)

                        // Rear doors
                        rr(Color.Black.copy(alpha = 0.22f), 29f, 70f, 2f, 17f, 1f)

                        // Rear bumper
                        rr(Color(0xFF687581), 8f, 91f, 44f, 5f, 2f)

                        // Rear lights
                        rr(red, 8f, 84f, 7f, 8f, 2f)
                        rr(red, 45f, 84f, 7f, 8f, 2f)

                        // Front bumper
                        rr(Color(0xFF737F8A), 8f, 3f, 44f, 5f, 2f)

                        // Front grille
                        rr(black, 19f, 5f, 22f, 5f, 2f)
                        rr(metal, 23f, 7f, 14f, 1f, 1f)

                        // Headlights
                        rr(white, 9f, 7f, 10f, 7f, 2f)
                        rr(white, 41f, 7f, 10f, 7f, 2f)

                        // Indicators
                        rr(Color(0xFFFFB347), 9f, 16f, 5f, 3f, 1f)
                        rr(Color(0xFFFFB347), 46f, 16f, 5f, 3f, 1f)

                        // Mirrors
                        rr(Color(0xFF151B23), 2f, 28f, 8f, 7f, 3f)
                        rr(Color(0xFF151B23), 50f, 28f, 8f, 7f, 3f)
                        rr(Color(0xFF92A2AF), 3f, 30f, 5f, 2f, 1f)
                        rr(Color(0xFF92A2AF), 52f, 30f, 5f, 2f, 1f)
                    }

                    // ====================================================
                    // TRUCKS AND PICKUPS
                    // ====================================================

                    VehicleStyle.TRUCK -> {

                        // Cargo bed
                        grad(
                            Color(0xFF929EAA),
                            Color(0xFF354655),
                            7f, 5f, 46f, 49f, 5f
                        )

                        // Bed frame
                        rr(Color(0xFFB1BCC5), 8f, 6f, 44f, 3f, 1f)
                        rr(Color(0xFFB1BCC5), 8f, 50f, 44f, 3f, 1f)
                        rr(Color(0xFF65727F), 8f, 9f, 3f, 40f, 1f)
                        rr(Color(0xFF65727F), 49f, 9f, 3f, 40f, 1f)

                        // Cargo bed floor
                        rr(Color(0xFF303D4A), 12f, 11f, 36f, 36f, 2f)

                        for (i in 0..5) {
                            rr(
                                Color(0xFF82909C),
                                14f + i * 6f,
                                13f,
                                2f,
                                32f,
                                1f
                            )
                        }

                        // Cargo divider
                        rr(Color(0xFFB8C2CB), 10f, 51f, 40f, 3f, 1f)

                        // Cabin
                        grad(
                            Color(0xFF768696),
                            color,
                            7f, 55f, 46f, 42f, 7f
                        )

                        // Front cabin roof
                        grad(
                            color.copy(alpha = 0.95f),
                            color.copy(alpha = 0.7f),
                            10f, 58f, 40f, 9f, 4f
                        )

                        // Cabin windshield
                        grad(
                            Color(0xFFA5D9EB),
                            Color(0xFF244A62),
                            12f, 67f, 36f, 22f, 5f
                        )

                        // Windshield reflection
                        ln(Color.White.copy(alpha = 0.65f), 16f, 69f, 24f, 86f, 1.5f)
                        ln(Color.White.copy(alpha = 0.25f), 21f, 69f, 27f, 84f)

                        // Windshield separator
                        rr(Color(0xFF18232E), 29f, 68f, 2f, 21f, 1f)

                        // Side windows
                        grad(Color(0xFF8BC9DF), glassDark, 8f, 67f, 5f, 19f, 2f)
                        grad(Color(0xFF8BC9DF), glassDark, 47f, 67f, 5f, 19f, 2f)

                        // Door outlines
                        ln(Color.Black.copy(alpha = 0.35f), 8f, 63f, 8f, 92f)
                        ln(Color.Black.copy(alpha = 0.35f), 52f, 63f, 52f, 92f)

                        // Door handles
                        rr(silver, 9f, 83f, 4f, 2f, 1f)
                        rr(silver, 47f, 83f, 4f, 2f, 1f)

                        // Mirrors
                        rr(black, 2f, 65f, 8f, 7f, 3f)
                        rr(black, 50f, 65f, 8f, 7f, 3f)

                        // Front grille
                        rr(black, 17f, 90f, 26f, 6f, 2f)

                        for (i in 0..3) {
                            rr(Color(0xFF9BA8B5), 20f + i * 5f, 91f, 2f, 3f, 1f)
                        }

                        // Heavy-duty front bumper
                        rr(Color(0xFFB5C0C9), 7f, 95f, 46f, 5f, 2f)
                        rr(Color(0xFF65727F), 12f, 96f, 36f, 1f, 1f)

                        // Headlights at the front of the truck
                        rr(white, 8f, 89f, 11f, 6f, 2f)
                        rr(white, 41f, 89f, 11f, 6f, 2f)

                        rr(Color(0xFFFFB344), 9f, 88f, 5f, 2f, 1f)
                        rr(Color(0xFFFFB344), 46f, 88f, 5f, 2f, 1f)

                        // Rear tail lamps
                        rr(red, 7f, 6f, 5f, 9f, 2f)
                        rr(red, 48f, 6f, 5f, 9f, 2f)

                        // Cargo details
                        if (variant % 2 == 0) {
                            rr(Color(0xFF202833), 17f, 20f, 26f, 18f, 3f)
                            rr(Color(0xFF697987), 20f, 23f, 20f, 12f, 2f)
                        } else {
                            for (i in 0..4) {
                                rr(Color(0xFF8996A1), 14f, 17f + i * 5f, 32f, 2f, 1f)
                            }
                        }
                    }

                    VehicleStyle.MOTORCYCLE -> Unit
                }

                // --------------------------------------------------------
                // COMMON MIRRORS
                // --------------------------------------------------------

                if (style != VehicleStyle.TRUCK && style != VehicleStyle.VAN) {
                    rr(Color(0xFF101720), 0f, 30f, 10f, 9f, 4f)
                    rr(Color(0xFF101720), 50f, 30f, 10f, 9f, 4f)
                    rr(Color(0xFF92A7B5), 2f, 32f, 5f, 3f, 2f)
                    rr(Color(0xFF92A7B5), 53f, 32f, 5f, 3f, 2f)
                }

                // --------------------------------------------------------
                // SIDE INDICATORS
                // --------------------------------------------------------

                if (style != VehicleStyle.TRUCK) {
                    rr(Color(0xFFFFB84A), 6f, 35f, 4f, 2f, 1f)
                    rr(Color(0xFFFFB84A), 50f, 35f, 4f, 2f, 1f)
                }

                // --------------------------------------------------------
                // COMMON FRONT AND REAR DETAILS
                // --------------------------------------------------------

                if (style != VehicleStyle.TRUCK) {

                    // Front grille
                    if (style != VehicleStyle.SPORT) {
                        rr(Color(0xFF171D25), 18f, 4f, 24f, 5f, 2f)
                        rr(Color(0xFF75818D), 21f, 7f, 18f, 1f, 1f)

                        if (variant % 2 == 0) {
                            for (i in 0..3) {
                                rr(Color(0xFF64717E), 22f + i * 5f, 5f, 1f, 3f, 1f)
                            }
                        }
                    }

                    // Headlights
                    if (style != VehicleStyle.SPORT) {
                        rr(white, 9f, 5f, 12f, 6f, 2f)
                        rr(white, 39f, 5f, 12f, 6f, 2f)

                        rr(Color(0xFFB8E9FF), 12f, 7f, 6f, 2f, 1f)
                        rr(Color(0xFFB8E9FF), 42f, 7f, 6f, 2f, 1f)
                    }

                    // Front bumper
                    rr(
                        if (style == VehicleStyle.SPORT) Color(0xFF202833)
                        else Color(0xFF929DA7),
                        10f, 2f, 40f, 4f, 2f
                    )

                    // Rear bumper
                    rr(Color(0xFF87939E), 10f, 94f, 40f, 4f, 2f)

                    // Tail light housings
                    rr(Color(0xFF21151B), 9f, 88f, 15f, 6f, 2f)
                    rr(Color(0xFF21151B), 36f, 88f, 15f, 6f, 2f)

                    // LED tail lights
                    rr(red, 11f, 89f, 11f, 2f, 1f)
                    rr(red, 38f, 89f, 11f, 2f, 1f)

                    // Reverse lights
                    rr(Color(0xFFFFEEEE), 14f, 92f, 5f, 1f, 1f)
                    rr(Color(0xFFFFEEEE), 41f, 92f, 5f, 1f, 1f)
                }

                // --------------------------------------------------------
                // ADDITIONAL VARIANT DETAILS
                // --------------------------------------------------------

                // Racing stripes
                if (style == VehicleStyle.SPORT && variant % 3 == 0) {
                    rr(Color.White.copy(alpha = 0.4f), 28f, 10f, 2f, 17f, 1f)
                    rr(Color.White.copy(alpha = 0.35f), 28f, 49f, 2f, 12f, 1f)
                }

                // Metallic side trim
                if (style == VehicleStyle.SEDAN || style == VehicleStyle.SUV) {
                    rr(Color(0xFFB9C4CD).copy(alpha = 0.55f), 9f, 57f, 2f, 15f, 1f)
                    rr(Color(0xFFB9C4CD).copy(alpha = 0.55f), 49f, 57f, 2f, 15f, 1f)
                }

                // Sport side skirts
                if (style == VehicleStyle.SPORT) {
                    rr(Color(0xFF111820), 7f, 61f, 5f, 11f, 2f)
                    rr(Color(0xFF111820), 48f, 61f, 5f, 11f, 2f)

                    rr(Color(0xFF697581), 8f, 65f, 3f, 1f, 1f)
                    rr(Color(0xFF697581), 49f, 65f, 3f, 1f, 1f)
                }

                // Subtle paint reflections
                rr(
                    Color.White.copy(alpha = 0.12f),
                    23f, 12f, 3f, 15f, 1f
                )

                rr(
                    Color.White.copy(alpha = 0.1f),
                    34f, 12f, 3f, 15f, 1f
                )

                // Small center badge
                if (style == VehicleStyle.SPORT || style == VehicleStyle.SEDAN) {
                    rr(Color(0xFFE3E9EF), 28f, 5f, 4f, 2f, 1f)
                    dot(Color(0xFF46515D), 30f, 6f, 0.7f)
                }

                // Small rear license plate
                rr(Color(0xFFE5E9EC), 23f, 95f, 14f, 2f, 1f)
                rr(Color(0xFF424D58), 26f, 95.5f, 8f, 1f, 0.5f)

                // Door and body panel highlights
                if (style != VehicleStyle.VAN && style != VehicleStyle.TRUCK) {
                    ln(
                        Color.White.copy(alpha = 0.18f),
                        11f, 50f, 13f, 68f,
                        0.8f
                    )

                    ln(
                        Color.White.copy(alpha = 0.18f),
                        49f, 50f, 47f, 68f,
                        0.8f
                    )
                }

                // Player accent, if this renderer is ever used for the player
                if (player) {
                    rr(color.copy(alpha = 0.95f), 15f, 48f, 30f, 2f, 1f)
                }
            }

            // ENEMY VEHICLES
            enemies.forEach { enemy ->
                drawVehicle(
                    lane = enemy.lane,
                    y = enemy.y,
                    color = enemy.color,
                    model = enemy.model
                )
            }

            // PLAYER VEHICLE
            val playerSize = vehicleSize(playerModel)
            val laneWidth = 340f / 3f

            val playerCenter =
                30f + playerLane * laneWidth + laneWidth / 2f

            val playerY = 555f

            if (!gameOver || crashProgress < 0.12f) {
                drawVehicle(
                    lane = playerLane,
                    y = playerY,
                    color = playerPaint,
                    model = playerModel,
                    player = true
                )
            }

            // CRASH EFFECT
            if (gameOver) {
                val cx = playerCenter
                val cy = playerY + playerSize.second / 2f
                val p = crashProgress

                circle(
                    Yellow.copy(alpha = (1f - p) * 0.85f),
                    cx, cy, 12f + p * 90f
                )

                circle(
                    Color(0xFFFF5A20).copy(alpha = 1f - p),
                    cx, cy, 8f + p * 55f
                )

                if (p < 0.75f) {
                    circle(
                        Yellow.copy(alpha = 1f - p),
                        cx, cy, 25f * (1f - p) + 5f
                    )

                    circle(
                        Color.White.copy(alpha = 1f - p),
                        cx, cy, 10f * (1f - p)
                    )
                }

                // Particles
                for (i in 0..17) {
                    val angle = i * 0.349f
                    val distance = 25f + p * 115f

                    val px = cx + cos(angle) * distance
                    val py = cy + sin(angle) * distance

                    val particleColor = when (i % 4) {
                        0 -> Yellow
                        1 -> Color(0xFFFF5A20)
                        2 -> Color(0xFFECEFF1)
                        else -> Color(0xFF68717D)
                    }

                    rect(
                        particleColor.copy(alpha = 1f - p),
                        px - 4f, py - 4f, 8f, 8f
                    )
                }

                // Smoke
                for (i in 0..7) {
                    val offset = i * 17f

                    circle(
                        Color(0xFF69717A).copy(alpha = p * 0.55f),
                        cx + cos(i * 1.7f) * offset,
                        cy - p * 45f + sin(i * 1.7f) * offset,
                        9f + p * 15f
                    )
                }

                // Flash
                if (p < 0.16f) {
                    rect(
                        Color.White.copy(alpha = (0.16f - p) * 3f),
                        0f, 0f, 400f, 700f
                    )
                }
            }
        }

        // HUD
        if (hasStarted && !showMenu && !showSplash) Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 27.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PIXEL RUSH",
                color = Yellow,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HudCard("SCORE", "$score", Yellow)
                HudCard("BEST", "$bestScore", Mint)
                HudCard("SPEED", "${gameSpeed.toInt()}", Color(0xFFFF8A65))
            }
        }

        // GAME OVER
        if (gameOver && crashFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE9000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(17.dp)
                ) {
                    Text(
                        "GAME OVER",
                        color = Color(0xFFFF5555),
                        fontSize = 37.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )

                    Text(
                        "YOUR SCORE",
                        color = Color.LightGray,
                        fontSize = 15.sp
                    )

                    Text(
                        "$score",
                        color = Yellow,
                        fontSize = 55.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        "BEST SCORE: $bestScore",
                        color = Mint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { restart() },
                        modifier = Modifier
                            .width(215.dp)
                            .height(62.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Mint
                        )
                    ) {
                        Text(
                            "↻  RETRY",
                            color = Color.Black,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            hasStarted = false
                            showMenu = true
                            showGarage = false
                        },
                        modifier = Modifier.width(215.dp)
                    ) {
                        Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // First-launch intro. Place logo.png in app/src/main/res/drawable-nodpi/logo.png.
        if (showSplash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101722)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Game logo",
                        modifier = Modifier.size(176.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "CREATED BY AMIR ERFAN ESLAMIKIA",
                        color = Yellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        } else if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF07111F), Color(0xFF142B49),
                                Color(0xFF241A46), Color(0xFF07111F)
                            )
                        )
                    )
            ) {
                // Soft neon light pools behind the menu.
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF19DCC7).copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.12f, size.height * 0.18f),
                            radius = size.width * 0.62f
                        ),
                        radius = size.width * 0.62f,
                        center = Offset(size.width * 0.12f, size.height * 0.18f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF8D5BFF).copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(size.width * 0.92f, size.height * 0.72f),
                            radius = size.width * 0.72f
                        ),
                        radius = size.width * 0.72f,
                        center = Offset(size.width * 0.92f, size.height * 0.72f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        if (showGarage) "THE GARAGE" else "PIXEL RUSH",
                        color = Color.White,
                        fontSize = if (showGarage) 29.sp else 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Box(
                        modifier = Modifier
                            .width(112.dp)
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF19E6CF), Color(0xFF8B5CFF), Color(0xFFFFD447)
                                    )),
                                RoundedCornerShape(50)
                            )
                    )
                    Text(
                        if (showGarage) "BUILD YOUR DREAM LINEUP" else "NEON STREETS. INFINITE SPEED.",
                        color = Color(0xFF9CB8D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    if (!showGarage) {
                        // Hero stats panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xCC15283D)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43DCCB).copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("PERSONAL RECORD", color = Color(0xFF9CB8D4), fontSize = 10.sp, letterSpacing = 1.8.sp)
                                    Text("$bestScore", color = Color(0xFFFFD447), fontSize = 34.sp, fontWeight = FontWeight.Black)
                                    Text("BEST SCORE", color = Color(0xFF6F8AA7), fontSize = 9.sp, letterSpacing = 1.5.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("YOUR RIDE", color = Color(0xFF9CB8D4), fontSize = 10.sp, letterSpacing = 1.5.sp)
                                    Text(playerModel.name.replace('_', ' '), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("${unlockedCarCount}/${garageCars.size} CARS UNLOCKED", color = Color(0xFF19E6CF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Selected ride showcase
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xAA0C1A2B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, playerPaint.copy(alpha = 0.65f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("CURRENTLY SELECTED", color = Color(0xFF8FAAC4), fontSize = 9.sp, letterSpacing = 2.sp)
                                Spacer(Modifier.height(6.dp))
                                Text("🏎", fontSize = 42.sp)
                                Text(playerModel.name.replace('_', ' '), color = playerPaint, fontSize = 17.sp, fontWeight = FontWeight.Black)
                                Text("CUSTOM PAINT • READY TO RACE", color = Color(0xFF8FAAC4), fontSize = 9.sp, letterSpacing = 1.sp)
                            }
                        }

                        Button(
                            onClick = { restart() },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF19DCC7)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp, pressedElevation = 2.dp)
                        ) {
                            Text("▶   START RACING", color = Color(0xFF06131F), fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                        Button(
                            onClick = { showGarage = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C55D9)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text("✦   CAR COLLECTION  •  $unlockedCarCount/${garageCars.size}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        OutlinedButton(
                            onClick = { (context as? Activity)?.finish() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Text("EXIT GAME", color = Color(0xFFB7C7D8), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                        Text("DODGE TRAFFIC  •  BREAK YOUR RECORD  •  UNLOCK CARS", color = Color(0xFF7189A4), fontSize = 9.sp, letterSpacing = 1.sp)
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xCC15283D)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43DCCB).copy(alpha = 0.45f))
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("BEST SCORE", color = Color(0xFF9CB8D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("$bestScore  •  $unlockedCarCount/${garageCars.size} UNLOCKED", color = Color(0xFF19E6CF), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Earn a new vehicle every $unlockStep best-score points. Select any unlocked ride.", color = Color(0xFFB8C9DA), fontSize = 12.sp)
                            }
                        }

                        garageCars.chunked(2).forEachIndexed { rowIndex, rowCars ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowCars.forEachIndexed { columnIndex, car ->
                                    val index = rowIndex * 2 + columnIndex
                                    val requiredScore = index * unlockStep
                                    val unlocked = index < unlockedCarCount
                                    val selected = index == activeCarIndex
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(112.dp)
                                            .clickable(enabled = unlocked) {
                                                selectedCarIndex = index
                                                preferences.edit().putInt("selected_car", index).apply()
                                                showGarage = false
                                            },
                                        shape = RoundedCornerShape(17.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                selected -> Color(0xFF174B50)
                                                unlocked -> Color(0xCC182D43)
                                                else -> Color(0xAA101B2A)
                                            }
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (selected) Color(0xFF19E6CF) else Color(0xFF526B85).copy(alpha = if (unlocked) 0.65f else 0.25f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(if (unlocked) "🏎" else "🏎🔒", fontSize = 25.sp)
                                            Text(
                                                car.name.replace('_', ' '),
                                                color = if (unlocked) Color.White else Color(0xFF718096),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                when {
                                                    selected -> "EQUIPPED"
                                                    unlocked -> "TAP TO SELECT"
                                                    else -> "BEST: $requiredScore"
                                                },
                                                color = if (selected) Color(0xFF19E6CF) else Color(0xFF91A8C0),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                if (rowCars.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }

                        OutlinedButton(
                            onClick = { showGarage = false },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF19DCC7).copy(alpha = 0.7f))
                        ) {
                            Text("←   BACK TO PIXEL RUSH", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}


/**
 * Premium top-down Lamborghini-inspired player car.
 * Coordinates use the same 400 x 700 virtual canvas as GameScreen.
 */
private fun DrawScope.drawLamborghini(center: Float, y: Float) {
    val sx = size.width / 400f
    val sy = size.height / 700f
    val carWidth = 60f
    val left = center - carWidth / 2f

    fun rounded(
        color: Color,
        x: Float,
        top: Float,
        width: Float,
        height: Float,
        radius: Float = 3f
    ) {
        drawRoundRect(
            color = color,
            topLeft = Offset(x * sx, top * sy),
            size = Size(width * sx, height * sy),
            cornerRadius = CornerRadius(radius * sx, radius * sy)
        )
    }

    fun gradient(
        topColor: Color,
        bottomColor: Color,
        x: Float,
        top: Float,
        width: Float,
        height: Float,
        radius: Float = 4f
    ) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor),
                startY = top * sy,
                endY = (top + height) * sy
            ),
            topLeft = Offset(x * sx, top * sy),
            size = Size(width * sx, height * sy),
            cornerRadius = CornerRadius(radius * sx, radius * sy)
        )
    }

    fun line(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, stroke: Float = 1f) {
        drawLine(
            color = color,
            start = Offset(x1 * sx, y1 * sy),
            end = Offset(x2 * sx, y2 * sy),
            strokeWidth = stroke * sx
        )
    }

    fun carX(localX: Float) = left + localX

    val black = Color(0xFF080C12)
    val carbon = Color(0xFF171E27)
    val yellowTop = Color(0xFFFFF078)
    val yellow = Color(0xFFFFD21F)
    val yellowShade = Color(0xFFE89B08)
    val glass = Color(0xFF16384D)
    val glassLight = Color(0xFF8DD5ED)

    // Ground shadow and dark underbody
    rounded(Color.Black.copy(alpha = 0.48f), carX(2f), y + 3f, 56f, 84f, 12f)
    rounded(black, carX(5f), y + 3f, 50f, 81f, 12f)

    // Wide performance tires and detailed rims
    for (wheelY in listOf(14f, 60f)) {
        rounded(black, carX(0f), y + wheelY, 12f, 19f, 4f)
        rounded(black, carX(48f), y + wheelY, 12f, 19f, 4f)
        rounded(Color(0xFF46515D), carX(2f), y + wheelY + 3f, 3f, 13f, 1f)
        rounded(Color(0xFF46515D), carX(55f), y + wheelY + 3f, 3f, 13f, 1f)
        rounded(Color(0xFFC5D0D9), carX(4f), y + wheelY + 5f, 5f, 9f, 2f)
        rounded(Color(0xFFC5D0D9), carX(51f), y + wheelY + 5f, 5f, 9f, 2f)
        rounded(Color(0xFF65717D), carX(5.5f), y + wheelY + 8f, 2f, 3f, 1f)
        rounded(Color(0xFF65717D), carX(52.5f), y + wheelY + 8f, 2f, 3f, 1f)
        line(Color.White.copy(alpha = 0.55f), carX(4f), y + wheelY + 5f, carX(8f), y + wheelY + 5f, 0.7f)
        line(Color.White.copy(alpha = 0.55f), carX(52f), y + wheelY + 5f, carX(56f), y + wheelY + 5f, 0.7f)
    }

    // Main angular body shell
    gradient(yellowTop, yellow, carX(7f), y + 1f, 46f, 84f, 13f)
    rounded(yellowShade.copy(alpha = 0.8f), carX(7f), y + 18f, 3f, 48f, 2f)
    rounded(Color(0xFFFFEE72), carX(50f), y + 18f, 3f, 48f, 2f)

    // Front splitter, bumper and aggressive air intakes
    rounded(Color(0xFF252B32), carX(5f), y + 4f, 50f, 7f, 3f)
    rounded(black, carX(11f), y + 6f, 12f, 4f, 2f)
    rounded(black, carX(37f), y + 6f, 12f, 4f, 2f)
    rounded(Color(0xFF687580), carX(13f), y + 7f, 8f, 1f, 1f)
    rounded(Color(0xFF687580), carX(39f), y + 7f, 8f, 1f, 1f)
    rounded(Color(0xFF111820), carX(22f), y + 5f, 16f, 5f, 2f)
    rounded(Color(0xFF89949D), carX(25f), y + 7f, 10f, 1f, 1f)

    // Sharp LED headlights
    rounded(Color(0xFF252B31), carX(8f), y + 12f, 14f, 5f, 2f)
    rounded(Color(0xFFFFF5C5), carX(10f), y + 13f, 10f, 2f, 1f)
    rounded(Color(0xFF252B31), carX(38f), y + 12f, 14f, 5f, 2f)
    rounded(Color(0xFFFFF5C5), carX(40f), y + 13f, 10f, 2f, 1f)
    line(Color.White.copy(alpha = 0.8f), carX(11f), y + 13f, carX(18f), y + 14f, 0.7f)
    line(Color.White.copy(alpha = 0.8f), carX(42f), y + 14f, carX(49f), y + 13f, 0.7f)

    // Sculpted hood with raised center and aerodynamic creases
    gradient(Color(0xFFFFFFA0), Color(0xFFFFC51B), carX(12f), y + 17f, 36f, 18f, 8f)
    line(Color.White.copy(alpha = 0.65f), carX(14f), y + 19f, carX(23f), y + 31f, 1f)
    line(Color.White.copy(alpha = 0.65f), carX(46f), y + 19f, carX(37f), y + 31f, 1f)
    rounded(Color(0xFFFFF4A1).copy(alpha = 0.65f), carX(29f), y + 19f, 2f, 12f, 1f)
    line(yellowShade.copy(alpha = 0.65f), carX(11f), y + 29f, carX(20f), y + 35f, 1f)
    line(yellowShade.copy(alpha = 0.65f), carX(49f), y + 29f, carX(40f), y + 35f, 1f)

    // Dark panoramic windshield
    gradient(Color(0xFF9ADCF0), glass, carX(13f), y + 34f, 34f, 15f, 6f)
    rounded(Color(0xFF0A1C2A), carX(13f), y + 34f, 34f, 2f, 1f)
    line(Color.White.copy(alpha = 0.72f), carX(17f), y + 36f, carX(24f), y + 46f, 1.1f)
    line(Color(0xFF5C9CB7).copy(alpha = 0.8f), carX(43f), y + 36f, carX(37f), y + 46f, 1f)
    rounded(Color(0xFF101A24), carX(28.5f), y + 35f, 3f, 13f, 1f)

    // Roof panel and subtle roof highlights
    gradient(Color(0xFFFFE45A), Color(0xFFE7A90D), carX(16f), y + 48f, 28f, 13f, 5f)
    line(Color.White.copy(alpha = 0.35f), carX(19f), y + 50f, carX(24f), y + 58f, 0.8f)
    rounded(Color(0xFFB77B0B).copy(alpha = 0.6f), carX(18f), y + 59f, 24f, 1.5f, 1f)

    // Rear engine cover, vents and side air channels
    gradient(Color(0xFF547D91), Color(0xFF142B3B), carX(17f), y + 62f, 26f, 9f, 3f)
    for (i in 0..4) {
        rounded(Color(0xFF07121B), carX(19f + i * 4.5f), y + 63f, 2f, 7f, 1f)
    }
    rounded(Color(0xFF111820), carX(8f), y + 48f, 6f, 13f, 2f)
    rounded(Color(0xFF111820), carX(46f), y + 48f, 6f, 13f, 2f)
    for (i in 0..2) {
        rounded(Color(0xFF77838D), carX(9f), y + 51f + i * 3f, 4f, 1f, 1f)
        rounded(Color(0xFF77838D), carX(47f), y + 51f + i * 3f, 4f, 1f, 1f)
    }

    // Side skirts and sharp body lines
    line(Color.White.copy(alpha = 0.55f), carX(8f), y + 36f, carX(10f), y + 66f, 1f)
    line(Color.White.copy(alpha = 0.55f), carX(52f), y + 36f, carX(50f), y + 66f, 1f)
    rounded(Color(0xFF171D24), carX(5f), y + 67f, 50f, 4f, 1f)

    // Rear deck, continuous LED tail light and diffuser
    gradient(Color(0xFFFFD94A), Color(0xFFE49B08), carX(11f), y + 71f, 38f, 8f, 4f)
    rounded(Color(0xFF1B1118), carX(9f), y + 73f, 42f, 4f, 2f)
    rounded(Color(0xFFFF3348), carX(11f), y + 74f, 38f, 2f, 1f)
    rounded(Color(0xFFFFA0A0), carX(13f), y + 74f, 8f, 1f, 1f)
    rounded(Color(0xFFFFA0A0), carX(39f), y + 74f, 8f, 1f, 1f)
    rounded(black, carX(7f), y + 79f, 46f, 6f, 2f)
    for (i in 0..4) {
        rounded(Color(0xFF687582), carX(15f + i * 7f), y + 80f, 2f, 4f, 1f)
    }
    rounded(Color(0xFF080C12), carX(14f), y + 82f, 7f, 3f, 1f)
    rounded(Color(0xFF080C12), carX(39f), y + 82f, 7f, 3f, 1f)

    // Small golden reflections on the fenders
    line(Color.White.copy(alpha = 0.45f), carX(9f), y + 22f, carX(12f), y + 30f, 0.8f)
    line(Color.White.copy(alpha = 0.45f), carX(51f), y + 22f, carX(48f), y + 30f, 0.8f)
}

@Composable
private fun HudCard(
    title: String,
    value: String,
    color: Color
) {
    Surface(
        color = Color(0xE9101721),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(
            1.dp,
            color.copy(alpha = 0.5f),
            RoundedCornerShape(12.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = Color.LightGray,
                fontSize = 9.sp,
                letterSpacing = 1.5.sp
            )

            Text(
                value,
                color = color,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}