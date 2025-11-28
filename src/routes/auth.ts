import { Elysia, t } from "elysia";
import { db } from "../db";
import { users } from "../db/schema";
import { eq } from "drizzle-orm";
import bcrypt from "bcryptjs";

export const authRoutes = new Elysia({ prefix: "/auth" })
  // Login
  .post(
    "/login",
    async ({ body, set }) => {
      const { username, password } = body;

      const user = await db.query.users.findFirst({
        where: eq(users.username, username),
      });

      if (!user) {
        set.status = 401;
        return { success: false, message: "Invalid credentials" };
      }

      // Verify password (support both bcrypt and plain text for migration)
      let passwordMatch = false;
      
      // Check if password is hashed (bcrypt hashes start with $2b$)
      if (user.password.startsWith('$2b$') || user.password.startsWith('$2a$')) {
        // Bcrypt hashed password
        passwordMatch = await bcrypt.compare(password, user.password);
      } else {
        // Plain text password (old users)
        passwordMatch = user.password === password;
        
        // If login successful, upgrade to bcrypt
        if (passwordMatch) {
          const hashedPassword = await bcrypt.hash(password, 10);
          await db.update(users)
            .set({ password: hashedPassword })
            .where(eq(users.id, user.id));
        }
      }
      
      if (!passwordMatch) {
        set.status = 401;
        return { success: false, message: "Invalid credentials" };
      }

      if (!user.isActive) {
        set.status = 403;
        return { success: false, message: "Account is inactive" };
      }

      // Generate simple token (in production, use JWT)
      const token = Buffer.from(`${user.id}:${user.username}:${Date.now()}`).toString('base64');

      return {
        success: true,
        message: "Login successful",
        data: {
          user: {
            id: user.id,
            username: user.username,
            firstName: user.firstName,
            lastName: user.lastName,
            role: user.role,
            profilePhotoUri: user.profilePhotoUri,
            profileCompleted: user.profileCompleted,
          },
          token: token,
        },
      };
    },
    {
      body: t.Object({
        username: t.String(),
        password: t.String(),
      }),
    }
  )

  // Register (Email/Password signup)
  .post(
    "/register",
    async ({ body, set }) => {
      try {
        const { username, email, password, firstName, lastName, profilePictureUrl } = body;

        // Validate input
        if (!username || !email || !password || !firstName || !lastName) {
          set.status = 400;
          return { success: false, message: "Missing required fields: username, email, password, firstName, lastName" };
        }

        // Check if username exists
        const existingUsername = await db.query.users.findFirst({
          where: eq(users.username, username.toLowerCase()),
        });

        if (existingUsername) {
          set.status = 400;
          return { success: false, message: "Username already exists" };
        }

        // Check if email exists
        const existingEmail = await db.query.users.findFirst({
          where: eq(users.email, email.toLowerCase()),
        });

        if (existingEmail) {
          set.status = 400;
          return { success: false, message: "Email already exists" };
        }

        // Hash password with bcrypt
        const hashedPassword = await bcrypt.hash(password, 10);
        
        const [newUser] = await db
          .insert(users)
          .values({
            username: username.toLowerCase(),
            email: email.toLowerCase(),
            password: hashedPassword,
            firstName: firstName.trim(),
            lastName: lastName.trim(),
            profilePictureUrl: profilePictureUrl || null,
            role: "user",
            isActive: true,
            forcePasswordChange: false,
          })
          .returning();

        // Generate token
        const token = Buffer.from(`${newUser.id}:${newUser.username}:${Date.now()}`).toString('base64');

        return {
          success: true,
          message: "Registration successful",
          data: {
            user: {
              id: newUser.id,
              username: newUser.username,
              email: newUser.email,
              firstName: newUser.firstName,
              lastName: newUser.lastName,
              role: newUser.role,
              profilePictureUrl: newUser.profilePictureUrl,
            },
            token: token,
          },
        };
      } catch (error: any) {
        set.status = 500;
        return {
          success: false,
          message: "Registration failed",
          error: error.message,
        };
      }
    },
    {
      body: t.Object({
        username: t.String(),
        email: t.String(),
        password: t.String(),
        firstName: t.String(),
        lastName: t.String(),
        profilePictureUrl: t.Optional(t.String()),
      }),
    }
  )

  // Google Sign-In endpoint
  .post(
    "/google",
    async ({ body, set }) => {
      try {
        const { googleId, email, firstName, lastName, profilePictureUrl } = body;

        // Validate input
        if (!googleId || !email || !firstName || !lastName) {
          set.status = 400;
          return { success: false, message: "Missing required Google fields" };
        }

        // Check if user exists by Google ID or email
        let user = await db.query.users.findFirst({
          where: eq(users.googleId, googleId),
        });
        
        // If not found by googleId, try by email
        if (!user) {
          user = await db.query.users.findFirst({
            where: eq(users.email, email.toLowerCase()),
          });
        }

        if (user) {
          // Existing Google user - update last login
          const [updatedUser] = await db
            .update(users)
            .set({
              lastLogin: new Date(),
              profilePictureUrl: profilePictureUrl || user.profilePictureUrl,
            })
            .where(eq(users.id, user.id))
            .returning();

          const token = Buffer.from(`${updatedUser.id}:${updatedUser.username}:${Date.now()}`).toString('base64');

          return {
            success: true,
            message: "Google login successful",
            is_new: false,
            data: {
              user: {
                id: updatedUser.id,
                username: updatedUser.username,
                email: updatedUser.email,
                firstName: updatedUser.firstName,
                lastName: updatedUser.lastName,
                role: updatedUser.role,
                profilePictureUrl: updatedUser.profilePictureUrl,
              },
              token: token,
            },
          };
        } else {
          // New Google user - create account
          const username = `${firstName.toLowerCase()}${Math.floor(Math.random() * 10000)}`;

          console.log("📝 Creating new Google user:", { googleId, email, firstName, lastName });

          const [newUser] = await db
            .insert(users)
            .values({
              username: username,
              email: email.toLowerCase(),
              firstName: firstName.trim(),
              lastName: lastName.trim(),
              googleId: googleId || null, // Allow null if googleId is empty
              profilePictureUrl: profilePictureUrl || null,
              role: "user",
              isActive: true,
              lastLogin: new Date(),
            })
            .returning();
          
          console.log("✅ New Google user created:", newUser.id);

          const token = Buffer.from(`${newUser.id}:${newUser.username}:${Date.now()}`).toString('base64');

          return {
            success: true,
            message: "Google signup successful",
            is_new: true,
            data: {
              user: {
                id: newUser.id,
                username: newUser.username,
                email: newUser.email,
                firstName: newUser.firstName,
                lastName: newUser.lastName,
                role: newUser.role,
                profilePictureUrl: newUser.profilePictureUrl,
              },
              token: token,
            },
          };
        }
      } catch (error: any) {
        console.error("❌ Google Auth Error:", {
          message: error.message,
          code: error.code,
          detail: error.detail,
          stack: error.stack,
        });
        set.status = 500;
        return {
          success: false,
          message: "Google authentication failed",
          error: error.message,
          code: error.code,
          detail: error.detail,
        };
      }
    },
    {
      body: t.Object({
        googleId: t.String(),
        email: t.String(),
        firstName: t.String(),
        lastName: t.String(),
        profilePictureUrl: t.Optional(t.String()),
      }),
    }
  )
  
  // Update User Profile (Profile Photo & Completion)
  .put(
    "/profile/:userId",
    async ({ params, body, set }) => {
      try {
        const userId = parseInt(params.userId);
        const { profilePhotoUri, profileCompleted } = body;

        // Update user profile
        const [updatedUser] = await db
          .update(users)
          .set({
            profilePhotoUri: profilePhotoUri,
            profileCompleted: profileCompleted ?? true,
            updatedAt: new Date(),
          })
          .where(eq(users.id, userId))
          .returning();

        if (!updatedUser) {
          set.status = 404;
          return {
            success: false,
            message: "User not found",
          };
        }

        return {
          success: true,
          message: "Profile updated successfully",
          user: updatedUser,
        };
      } catch (error: any) {
        set.status = 500;
        return {
          success: false,
          message: "Failed to update profile",
          error: error.message,
        };
      }
    },
    {
      body: t.Object({
        profilePhotoUri: t.String(),
        profileCompleted: t.Optional(t.Boolean()),
      }),
    }
  );
