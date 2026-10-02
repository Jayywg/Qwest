package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.PasswordSecurity
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelCharacterSprite
import com.example.ui.components.PixelFire
import com.example.ui.theme.PixelBackground
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelBorderHighlight
import com.example.ui.theme.PixelEmerald
import com.example.ui.theme.PixelGold
import com.example.ui.theme.PixelGoldDark
import com.example.ui.theme.PixelGoldGlow
import com.example.ui.theme.PixelMana
import com.example.ui.theme.PixelRuby
import com.example.ui.theme.PixelSurface
import com.example.ui.theme.PixelSurfaceElevated
import com.example.ui.theme.PixelTextDim
import com.example.ui.theme.PixelTextMuted
import com.example.ui.theme.PixelTextParchment
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay

enum class AuthScreenMode {
    LANDING,
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@Composable
fun AuthRootScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var screenMode by remember { mutableStateOf(AuthScreenMode.LANDING) }
    val authLoading by viewModel.authLoading.collectAsState()
    val authLoadingMessage by viewModel.authLoadingMessage.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccessTransition by viewModel.authSuccessTransition.collectAsState()

    // Clear error when navigating between modes
    fun navigateTo(mode: AuthScreenMode) {
        viewModel.clearAuthError()
        screenMode = mode
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBackground)
    ) {
        val isWideScreen = maxWidth >= 680.dp

        if (isWideScreen) {
            // Tablet / Desktop Split Screen Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left hero panel with cozy tavern environment
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    AuthFantasyHeroScene()
                }

                // Right form container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(PixelBackground)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AuthFormCard(
                        screenMode = screenMode,
                        authLoading = authLoading,
                        authLoadingMessage = authLoadingMessage,
                        authError = authError,
                        authSuccessTransition = authSuccessTransition,
                        onNavigate = { navigateTo(it) },
                        viewModel = viewModel
                    )
                }
            }
        } else {
            // Mobile Compact Stacked Layout with Background
            Box(modifier = Modifier.fillMaxSize()) {
                // Ambient background banner with dark parchment scrim
                Image(
                    painter = painterResource(id = R.drawable.fantasy_tavern_banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xEE13111C),
                                    Color(0xF513111C),
                                    Color(0xFA13111C)
                                )
                            )
                        )
                )

                AuthFormCard(
                    screenMode = screenMode,
                    authLoading = authLoading,
                    authLoadingMessage = authLoadingMessage,
                    authError = authError,
                    authSuccessTransition = authSuccessTransition,
                    onNavigate = { navigateTo(it) },
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }

        // Celebratory Success Transition Overlay
        if (authSuccessTransition != null) {
            AuthSuccessOverlay(
                message = authSuccessTransition!!,
                onDismiss = { viewModel.clearAuthSuccessTransition() }
            )
        }
    }
}

@Composable
private fun AuthFormCard(
    screenMode: AuthScreenMode,
    authLoading: Boolean,
    authLoadingMessage: String,
    authError: String?,
    authSuccessTransition: String?,
    onNavigate: (AuthScreenMode) -> Unit,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.imePadding(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = screenMode,
            transitionSpec = {
                (slideInHorizontally(tween(350)) + fadeIn()).togetherWith(
                    slideOutHorizontally(tween(350)) + fadeOut()
                )
            },
            label = "auth_screen_transition"
        ) { mode ->
            when (mode) {
                AuthScreenMode.LANDING -> {
                    AuthLandingView(
                        onSignInClick = { onNavigate(AuthScreenMode.SIGN_IN) },
                        onCreateAccountClick = { onNavigate(AuthScreenMode.SIGN_UP) },
                        onDemoGuestClick = { viewModel.enterAsGuest() }
                    )
                }
                AuthScreenMode.SIGN_IN -> {
                    SignInView(
                        authLoading = authLoading,
                        authLoadingMessage = authLoadingMessage,
                        authError = authError,
                        onBack = { onNavigate(AuthScreenMode.LANDING) },
                        onNavigateSignUp = { onNavigate(AuthScreenMode.SIGN_UP) },
                        onNavigateForgotPassword = { onNavigate(AuthScreenMode.FORGOT_PASSWORD) },
                        onSubmit = { email, password ->
                            viewModel.signIn(email, password)
                        }
                    )
                }
                AuthScreenMode.SIGN_UP -> {
                    SignUpView(
                        authLoading = authLoading,
                        authLoadingMessage = authLoadingMessage,
                        authError = authError,
                        onBack = { onNavigate(AuthScreenMode.LANDING) },
                        onNavigateSignIn = { onNavigate(AuthScreenMode.SIGN_IN) },
                        onSubmit = { name, email, password, confirmPassword ->
                            viewModel.signUp(name, email, password, confirmPassword)
                        }
                    )
                }
                AuthScreenMode.FORGOT_PASSWORD -> {
                    ForgotPasswordView(
                        authLoading = authLoading,
                        onBack = { onNavigate(AuthScreenMode.SIGN_IN) },
                        onSubmit = { email, onResult ->
                            viewModel.sendPasswordReset(email, onResult)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 1. AUTHENTICATION LANDING SCREEN
 */
@Composable
fun AuthLandingView(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onDemoGuestClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "landing_idle")
    val characterBob by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_bob"
    )

    Column(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo / Title
        ParchmentBox(
            backgroundColor = PixelSurface,
            borderColor = PixelGoldDark,
            highlightBorder = true,
            contentPadding = 18.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Crest / Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PixelFire(size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚔️ QWEST",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        ),
                        color = PixelGoldGlow
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PixelFire(size = 18.dp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A Fantasy Habit-Tracking Adventure",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = PixelMana
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Character Display with Idle Bob
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PixelSurfaceElevated)
                        .border(1.5.dp, PixelBorderHighlight, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PixelCharacterSprite(
                        skinTone = "Fair",
                        hairStyle = "Short",
                        hairColor = "Brown",
                        outfitColor = "Red",
                        modifier = Modifier
                            .size(72.dp)
                            .padding(top = (characterBob + 4).dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tagline
                Text(
                    text = "\"Your journey begins with one Qwest.\"",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = PixelTextParchment,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Transform daily habits, tasks, and routines into heroic triumphs. Slay procrastination, level up your adventurer, and unlock legendary companions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // [ SIGN IN ]
                Button(
                    onClick = onSignInClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF13111C)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sign_in_nav_button")
                ) {
                    Text(
                        text = "SIGN IN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // [ CREATE ACCOUNT ]
                OutlinedButton(
                    onClick = onCreateAccountClick,
                    border = BorderStroke(1.5.dp, PixelGoldGlow),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PixelGoldGlow
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create_account_nav_button")
                ) {
                    Text(
                        text = "CREATE ACCOUNT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDemoGuestClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enter_as_guest_button")
                ) {
                    Text(
                        text = "⚡ Enter as Demo Adventurer (Guest)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelGoldGlow
                    )
                }
            }
        }
    }
}

/**
 * 2. SIGN IN SCREEN
 */
@Composable
fun SignInView(
    authLoading: Boolean,
    authLoadingMessage: String,
    authError: String?,
    onBack: () -> Unit,
    onNavigateSignUp: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onSubmit: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    fun validateAndSubmit() {
        focusManager.clearFocus()
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            localError = "Please enter both email and password."
            return
        }
        if (!PasswordSecurity.isValidEmail(trimmedEmail)) {
            localError = "That email doesn't look quite right."
            return
        }
        localError = null
        onSubmit(trimmedEmail, password)
    }

    Column(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Top row with Back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Welcome Screen",
                    tint = PixelGoldGlow
                )
            }
            Text(
                text = "Back",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = PixelGoldGlow,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ParchmentBox(
            backgroundColor = PixelSurface,
            borderColor = PixelBorder,
            contentPadding = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Text(
                    text = "QWEST",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    ),
                    color = PixelGoldGlow
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Welcome back, Adventurer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelTextParchment
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error announcement box
                val displayedError = localError ?: authError
                AnimatedVisibility(visible = displayedError != null) {
                    if (displayedError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixelRuby.copy(alpha = 0.15f))
                                .border(1.dp, PixelRuby, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = PixelRuby,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayedError,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = PixelRuby
                                )
                            }
                        }
                    }
                }

                // Email field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        placeholder = { Text("Enter your email", color = PixelTextDim) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PixelGold)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelGoldGlow,
                            unfocusedBorderColor = PixelBorder,
                            focusedContainerColor = PixelSurfaceElevated,
                            unfocusedContainerColor = PixelSurfaceElevated,
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Password field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        placeholder = { Text("Enter your password", color = PixelTextDim) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PixelGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = PixelTextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { validateAndSubmit() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelGoldGlow,
                            unfocusedBorderColor = PixelBorder,
                            focusedContainerColor = PixelSurfaceElevated,
                            unfocusedContainerColor = PixelSurfaceElevated,
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // [ SIGN IN ] Button
                Button(
                    onClick = { validateAndSubmit() },
                    enabled = !authLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF13111C),
                        disabledContainerColor = PixelSurfaceElevated,
                        disabledContentColor = PixelTextDim
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_in_submit_button")
                ) {
                    if (authLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = PixelGoldGlow,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = authLoadingMessage.ifEmpty { "Entering the Tavern..." },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PixelTextParchment
                            )
                        }
                    } else {
                        Text(
                            text = "SIGN IN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Demo Sign In Button
                TextButton(
                    onClick = {
                        email = "adventurer@qwest.com"
                        password = "Password123!"
                        localError = null
                        onSubmit("adventurer@qwest.com", "Password123!")
                    },
                    modifier = Modifier.testTag("quick_fill_demo_button")
                ) {
                    Text(
                        text = "⚡ Quick Sign In as Kaelen (Demo)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = PixelGoldGlow
                    )
                }

                // Forgot Password link
                TextButton(
                    onClick = onNavigateForgotPassword,
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = PixelMana
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Don't have an account? Create Account
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = PixelGoldGlow,
                        modifier = Modifier
                            .clickable { onNavigateSignUp() }
                            .padding(4.dp)
                            .testTag("navigate_to_signup_link")
                    )
                }
            }
        }
    }
}

/**
 * 3. SIGN UP SCREEN & 4. PASSWORD REQUIREMENTS
 */
@Composable
fun SignUpView(
    authLoading: Boolean,
    authLoadingMessage: String,
    authError: String?,
    onBack: () -> Unit,
    onNavigateSignIn: () -> Unit,
    onSubmit: (name: String, email: String, password: String, confirmPassword: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var adventurerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val requirements = PasswordSecurity.validatePassword(password)
    val isFormValid = adventurerName.trim().length in 2..24 &&
            PasswordSecurity.isValidEmail(email.trim()) &&
            requirements.isValid &&
            password == confirmPassword

    fun validateAndSubmit() {
        focusManager.clearFocus()
        val trimmedName = adventurerName.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isEmpty()) {
            localError = "Your adventure needs a name."
            return
        }
        if (!PasswordSecurity.isValidAdventurerName(trimmedName)) {
            localError = "Adventurer name must be 2 to 24 characters."
            return
        }
        if (!PasswordSecurity.isValidEmail(trimmedEmail)) {
            localError = "That email doesn't look quite right."
            return
        }
        if (!requirements.isValid) {
            localError = "Please satisfy all password requirements."
            return
        }
        if (password != confirmPassword) {
            localError = "Your passwords don't match."
            return
        }

        localError = null
        onSubmit(trimmedName, trimmedEmail, password, confirmPassword)
    }

    Column(
        modifier = modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Back
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Welcome Screen",
                    tint = PixelGoldGlow
                )
            }
            Text(
                text = "Back",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = PixelGoldGlow,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        ParchmentBox(
            backgroundColor = PixelSurface,
            borderColor = PixelBorder,
            contentPadding = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "QWEST",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    ),
                    color = PixelGoldGlow
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Begin your adventure.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelTextParchment
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Error Announcement
                val displayedError = localError ?: authError
                AnimatedVisibility(visible = displayedError != null) {
                    if (displayedError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixelRuby.copy(alpha = 0.15f))
                                .border(1.dp, PixelRuby, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = PixelRuby,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayedError,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = PixelRuby
                                )
                            }
                        }
                    }
                }

                // Adventurer Name
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Adventurer Name",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adventurerName,
                        onValueChange = {
                            if (it.length <= 24) adventurerName = it
                            localError = null
                        },
                        placeholder = { Text("Enter your hero's name", color = PixelTextDim) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PixelGold)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelGoldGlow,
                            unfocusedBorderColor = PixelBorder,
                            focusedContainerColor = PixelSurfaceElevated,
                            unfocusedContainerColor = PixelSurfaceElevated,
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adventurer_name_input")
                    )
                    Text(
                        text = "This will be your character's name in the Tavern.",
                        style = MaterialTheme.typography.labelSmall,
                        color = PixelTextDim,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        placeholder = { Text("Enter your email", color = PixelTextDim) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PixelGold)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelGoldGlow,
                            unfocusedBorderColor = PixelBorder,
                            focusedContainerColor = PixelSurfaceElevated,
                            unfocusedContainerColor = PixelSurfaceElevated,
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_email_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        placeholder = { Text("Create a password", color = PixelTextDim) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PixelGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = PixelTextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelGoldGlow,
                            unfocusedBorderColor = PixelBorder,
                            focusedContainerColor = PixelSurfaceElevated,
                            unfocusedContainerColor = PixelSurfaceElevated,
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_password_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section 4: Live Dynamic Password Requirements Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PixelSurfaceElevated)
                        .border(1.dp, PixelBorder, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Password must contain:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PixelTextMuted
                        )
                        PasswordRequirementItem(
                            isMet = requirements.hasMinLength,
                            label = "At least 8 characters"
                        )
                        PasswordRequirementItem(
                            isMet = requirements.hasUppercase,
                            label = "One uppercase letter"
                        )
                        PasswordRequirementItem(
                            isMet = requirements.hasNumber,
                            label = "One number"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Confirm Password",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            localError = null
                        },
                        placeholder = { Text("Confirm your password", color = PixelTextDim) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PixelGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = PixelTextMuted
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { validateAndSubmit() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword != password) PixelRuby else PixelGoldGlow,
                            unfocusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword != password) PixelRuby else PixelBorder,
                            focusedContainerColor = PixelSurfaceElevated,
                            unfocusedContainerColor = PixelSurfaceElevated,
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_confirm_password_input")
                    )
                    if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                        Text(
                            text = "Your passwords don't match.",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelRuby,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // [ CREATE ACCOUNT ] Button
                Button(
                    onClick = { validateAndSubmit() },
                    enabled = !authLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF13111C),
                        disabledContainerColor = PixelSurfaceElevated,
                        disabledContentColor = PixelTextDim
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_up_submit_button")
                ) {
                    if (authLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = PixelGoldGlow,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = authLoadingMessage.ifEmpty { "Preparing your adventure..." },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PixelTextParchment
                            )
                        }
                    } else {
                        Text(
                            text = "CREATE ACCOUNT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Already have an account? Sign In
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = PixelGoldGlow,
                        modifier = Modifier
                            .clickable { onNavigateSignIn() }
                            .padding(4.dp)
                            .testTag("navigate_to_signin_link")
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirementItem(
    isMet: Boolean,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isMet) PixelEmerald else PixelSurface)
                .border(1.dp, if (isMet) PixelEmerald else PixelTextDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isMet) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) PixelEmerald else PixelTextMuted
        )
    }
}

/**
 * 9. FORGOT PASSWORD
 */
@Composable
fun ForgotPasswordView(
    authLoading: Boolean,
    onBack: () -> Unit,
    onSubmit: (email: String, onResult: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var confirmationMessage by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Sign In",
                    tint = PixelGoldGlow
                )
            }
            Text(
                text = "Back to Sign In",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = PixelGoldGlow,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ParchmentBox(
            backgroundColor = PixelSurface,
            borderColor = PixelBorder,
            contentPadding = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RECOVER ACCOUNT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PixelGoldGlow
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Forgot your password?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PixelTextParchment
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter the email associated with your adventurer credentials. We'll send instructions to safely recover your journey.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (confirmationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PixelEmerald.copy(alpha = 0.15f))
                            .border(1.dp, PixelEmerald, RoundedCornerShape(6.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PixelEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reset Instructions Sent",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = confirmationMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = PixelTextParchment
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelGold,
                            contentColor = Color(0xFF13111C)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "RETURN TO SIGN IN",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    if (localError != null) {
                        Text(
                            text = localError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = PixelRuby,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PixelTextParchment
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                localError = null
                            },
                            placeholder = { Text("Enter your email", color = PixelTextDim) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = PixelGold)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PixelGoldGlow,
                                unfocusedBorderColor = PixelBorder,
                                focusedContainerColor = PixelSurfaceElevated,
                                unfocusedContainerColor = PixelSurfaceElevated,
                                focusedTextColor = PixelTextParchment,
                                unfocusedTextColor = PixelTextParchment
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_password_email_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val trimmed = email.trim()
                            if (!PasswordSecurity.isValidEmail(trimmed)) {
                                localError = "That email doesn't look quite right."
                                return@Button
                            }
                            localError = null
                            onSubmit(trimmed) { msg ->
                                confirmationMessage = msg
                            }
                        },
                        enabled = !authLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelGold,
                            contentColor = Color(0xFF13111C)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_reset_link_button")
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF13111C),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "SEND RESET LINK",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual background & side scene for wide/tablet displays
 */
@Composable
fun AuthFantasyHeroScene(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tavern_ambient")
    val hearthGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fantasy_tavern_banner),
            contentDescription = "The Cozy Tavern",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Warm hearth amber vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xBB13111C),
                            Color(0x77FB8500).copy(alpha = 0.15f * hearthGlow),
                            Color(0xEE13111C)
                        )
                    )
                )
        )

        // Overlay lore & adventurer
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelFire(size = 24.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "THE ROARING HEARTH",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = PixelGoldGlow
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rest your boots by the fire. Every habit mastered in real life forges your equipment, earns shining gold, and trains your faithful pets in the world of Qwest.",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelTextParchment,
                lineHeight = 22.sp,
                modifier = Modifier.widthIn(max = 420.dp)
            )
        }
    }
}

/**
 * 7. SUCCESSFUL SIGN UP / SIGN IN TRANSITION
 */
@Composable
fun AuthSuccessOverlay(
    message: String,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1400)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xDD13111C))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        ParchmentBox(
            backgroundColor = PixelSurface,
            borderColor = PixelGold,
            highlightBorder = true,
            contentPadding = 24.dp,
            modifier = Modifier.widthIn(max = 380.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelFire(size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WELCOME ADVENTURER!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = PixelGoldGlow
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PixelFire(size = 20.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                CircularProgressIndicator(
                    color = PixelGold,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = PixelTextParchment,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Entering the Tavern...",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelMana
                )
            }
        }
    }
}
